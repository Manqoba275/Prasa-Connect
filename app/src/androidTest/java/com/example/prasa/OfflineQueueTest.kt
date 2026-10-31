package com.example.prasa

import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class OfflineQueueTest {
    @Test fun queueSurvivesReopenAndIsAccountScoped() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val owner = UUID.randomUUID().toString()
        val other = UUID.randomUUID().toString()
        val id = UUID.randomUUID().toString()
        LocalStore(context).use { it.enqueue(owner,JSONObject().put("id",id).put("description","Persistent report")) }
        LocalStore(context).use {
            assertEquals(id,it.pending(owner).single().getString("id"))
            assertTrue(it.pending(other).isEmpty())
            it.acknowledge(id,other)
            assertEquals(1,it.pending(owner).size)
            it.acknowledge(id,owner)
            assertTrue(it.pending(owner).isEmpty())
        }
    }
}
