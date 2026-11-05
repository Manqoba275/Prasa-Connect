package com.example.prasa

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.ArrayDeque
import java.util.UUID

/** Intercepts HTTP locally; no live accounts or cloud writes are created by these tests. */
class RepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val responses = ArrayDeque<Pair<Int,String>>()
    private val requests = mutableListOf<Request>()
    private var originalSession:JSONObject? = null
    private val owner = UUID.randomUUID().toString()
    private val client = OkHttpClient.Builder().addInterceptor { chain ->
        requests.add(chain.request())
        val (code,body) = responses.removeFirst()
        Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
            .code(code).message("Test response").body(body.toResponseBody()).build()
    }.build()

    @Before fun prepare() {
        originalSession=SessionStore(context).load()
        SessionStore(context).clear()
    }
    @After fun restore() {
        originalSession?.let { SessionStore(context).save(it) } ?: SessionStore(context).clear()
    }
    private fun session(token:String="test-access",expiry:Long=System.currentTimeMillis()/1000+3600) =
        JSONObject().put("access_token",token).put("refresh_token","test-refresh")
            .put("expires_at",expiry).put("user",JSONObject().put("id",owner)).toString()

    @Test fun loginPersistsAndApiUsesBearerThenLogoutClears() = runBlocking {
        responses.add(200 to session())
        val repository=PrasaRepository(context,client)
        repository.login("test@example.com","test-password")
        assertTrue(PrasaRepository(context,client).signedIn)
        responses.add(200 to "{\"data\":[]}")
        repository.rows("/tickets")
        assertEquals("Bearer test-access",requests.last().header("Authorization"))
        responses.add(200 to "{}") // Device deregistration.
        responses.add(204 to "") // Auth logout.
        repository.logout()
        assertFalse(repository.signedIn)
        assertNull(SessionStore(context).load())
    }

    @Test fun rejectedLoginDoesNotPersistSession() = runBlocking {
        responses.add(400 to "{\"msg\":\"Invalid login credentials\"}")
        try {
            PrasaRepository(context,client).login("test@example.com","wrong-password")
            fail("Expected login rejection")
        } catch(e:ApiFailure) { assertEquals(400,e.status) }
        assertNull(SessionStore(context).load())
    }

    @Test fun expiredAccessTokenIsRefreshedBeforeDataRequest() = runBlocking {
        SessionStore(context).save(JSONObject(session(expiry=1)))
        responses.add(200 to session("refreshed-access"))
        responses.add(200 to "{\"data\":[]}")
        PrasaRepository(context,client).rows("/tickets")
        assertEquals("refresh_token",requests.first().url.queryParameter("grant_type"))
        assertEquals("Bearer refreshed-access",requests.last().header("Authorization"))
    }

    @Test fun rejectedReportIsNotMisreportedAsOfflineSuccess() = runBlocking {
        SessionStore(context).save(JSONObject(session()))
        responses.add(400 to "{\"message\":\"Rejected report\"}")
        val repository=PrasaRepository(context,client)
        try {
            repository.report("Safety","Station","A detailed incident report")
            fail("Expected API rejection")
        } catch(e:ApiFailure) { assertEquals(400,e.status) }
        assertTrue(repository.local.pending(owner).isEmpty())
    }

    @Test fun oldRepositoryCannotSendRequestsUnderAnotherAccount() = runBlocking {
        SessionStore(context).save(JSONObject(session()))
        val repository=PrasaRepository(context,client)
        val other=JSONObject(session()).put("user",JSONObject().put("id",UUID.randomUUID().toString()))
        SessionStore(context).save(other)
        try {
            repository.rows("/tickets")
            fail("Expected account-change rejection")
        } catch(e:ApiFailure) { assertEquals(401,e.status) }
        assertTrue(requests.isEmpty())
    }
}
