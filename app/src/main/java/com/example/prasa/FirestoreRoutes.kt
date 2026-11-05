package com.example.prasa

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/** Mirrors account-owned route favourites to Firestore while SQLite remains the offline source. */
class FirestoreRoutes {

    val isConfigured: Boolean
        get() = BuildConfig.FIREBASE_PROJECT_ID.isNotBlank() && BuildConfig.FIREBASE_API_KEY.isNotBlank()

    suspend fun signIn(repository: PrasaRepository) = withContext(Dispatchers.IO) {
        check(isConfigured) { "Firebase app settings are missing. Configure the PRASA Firebase build values." }
        check(runCatching { FirebaseApp.getInstance() }.isSuccess) { "Firebase is not initialized for this build." }
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser?.uid == repository.userId) return@withContext
        val token = repository.firebaseCustomToken()
        val result = Tasks.await(auth.signInWithCustomToken(token))
        check(result.user?.uid == repository.userId) { "Firebase account did not match the signed-in PRASA account." }
    }

    suspend fun save(owner: String, origin: String, destination: String) = withContext(Dispatchers.IO) {
        requireOwner(owner)
        val db = firestore()
        val route = mapOf("origin" to origin, "destination" to destination, "updatedAt" to com.google.firebase.Timestamp.now())
        Tasks.await(db.collection("users").document(owner).collection("saved_routes")
            .document(routeId(origin, destination)).set(route))
    }

    suspend fun remove(owner: String, origin: String, destination: String) = withContext(Dispatchers.IO) {
        requireOwner(owner)
        val db = firestore()
        Tasks.await(db.collection("users").document(owner).collection("saved_routes")
            .document(routeId(origin, destination)).delete())
    }

    suspend fun read(owner: String): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        requireOwner(owner)
        val db = firestore()
        Tasks.await(db.collection("users").document(owner).collection("saved_routes").get())
            .documents.mapNotNull { doc ->
                val origin = doc.getString("origin") ?: return@mapNotNull null
                val destination = doc.getString("destination") ?: return@mapNotNull null
                origin to destination
            }.sortedWith { left, right ->
                val originOrder = String.CASE_INSENSITIVE_ORDER.compare(left.first, right.first)
                if (originOrder != 0) originOrder else left.second.compareTo(right.second, ignoreCase = true)
            }
    }

    suspend fun sync(owner: String, local: LocalStore) = withContext(Dispatchers.IO) {
        requireOwner(owner)
        local.pendingRouteDeletions(owner).forEach { (origin, destination) ->
            remove(owner, origin, destination)
            local.acknowledgeRouteDeletion(owner, origin, destination)
        }
        local.routes(owner).forEach { (origin, destination) -> save(owner, origin, destination) }
        read(owner).forEach { (origin, destination) -> local.saveRoute(owner, origin, destination) }
    }

    fun signOut() { runCatching { FirebaseAuth.getInstance().signOut() } }

    // Firebase project uses a named Enterprise database rather than the default database.
    private fun firestore() = FirebaseFirestore.getInstance("prasa-app")

    private fun requireOwner(owner: String) {
        check(runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull() == owner) {
            "Sign in to the matching Firebase account before accessing saved routes."
        }
    }

    private fun routeId(origin: String, destination: String): String = MessageDigest.getInstance("SHA-256")
        .digest("${origin.trim().lowercase()}|${destination.trim().lowercase()}".toByteArray())
        .joinToString("") { "%02x".format(it) }
}
