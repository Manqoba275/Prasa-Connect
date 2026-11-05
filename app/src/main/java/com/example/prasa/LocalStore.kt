package com.example.prasa

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// Offline records are partitioned by account; logout cannot expose another user's cache.
class LocalStore(context: Context) : SQLiteOpenHelper(context, "prasa.db", null, 3), java.io.Closeable {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE cache (owner TEXT, name TEXT, body TEXT, PRIMARY KEY(owner,name))")
        db.execSQL("CREATE TABLE outbox (id TEXT PRIMARY KEY, owner TEXT NOT NULL, body TEXT NOT NULL)")
        createFavorites(db)
        createFavoriteDeletions(db)
    }
    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        if (old<2) createFavorites(db)
        if (old<3) createFavoriteDeletions(db)
    }
    private fun createFavorites(db:SQLiteDatabase) {
        db.execSQL("CREATE TABLE favorites (owner TEXT NOT NULL, origin TEXT NOT NULL, destination TEXT NOT NULL, PRIMARY KEY(owner,origin,destination))")
    }
    private fun createFavoriteDeletions(db:SQLiteDatabase) {
        db.execSQL("CREATE TABLE favorite_deletions (owner TEXT NOT NULL, origin TEXT NOT NULL, destination TEXT NOT NULL, PRIMARY KEY(owner,origin,destination))")
    }
    fun saveRoute(owner:String,origin:String,destination:String) {
        require(owner.isNotBlank() && origin.isNotBlank() && destination.isNotBlank())
        writableDatabase.beginTransaction()
        try {
            writableDatabase.insertWithOnConflict("favorites",null,ContentValues().apply {
                put("owner",owner); put("origin",origin); put("destination",destination)
            },SQLiteDatabase.CONFLICT_IGNORE)
            writableDatabase.delete("favorite_deletions","owner=? AND origin=? AND destination=?",arrayOf(owner,origin,destination))
            writableDatabase.setTransactionSuccessful()
        } finally { writableDatabase.endTransaction() }
    }
    fun routes(owner:String):List<Pair<String,String>> =
        readableDatabase.rawQuery("SELECT origin,destination FROM favorites WHERE owner=? ORDER BY origin,destination",arrayOf(owner)).use { cursor ->
            buildList { while(cursor.moveToNext()) add(cursor.getString(0) to cursor.getString(1)) }
        }
    fun removeRoute(owner:String,origin:String,destination:String) {
        writableDatabase.beginTransaction()
        try {
            writableDatabase.delete("favorites","owner=? AND origin=? AND destination=?",arrayOf(owner,origin,destination))
            writableDatabase.insertWithOnConflict("favorite_deletions",null,ContentValues().apply {
                put("owner",owner); put("origin",origin); put("destination",destination)
            },SQLiteDatabase.CONFLICT_REPLACE)
            writableDatabase.setTransactionSuccessful()
        } finally { writableDatabase.endTransaction() }
    }
    fun pendingRouteDeletions(owner:String):List<Pair<String,String>> =
        readableDatabase.rawQuery("SELECT origin,destination FROM favorite_deletions WHERE owner=?",arrayOf(owner)).use { cursor ->
            buildList { while(cursor.moveToNext()) add(cursor.getString(0) to cursor.getString(1)) }
        }
    fun acknowledgeRouteDeletion(owner:String,origin:String,destination:String) {
        writableDatabase.delete("favorite_deletions","owner=? AND origin=? AND destination=?",arrayOf(owner,origin,destination))
    }
    fun cache(owner: String, name: String, body: String) {
        writableDatabase.insertWithOnConflict("cache", null, ContentValues().apply {
            put("owner", owner); put("name", name); put("body", body)
        }, SQLiteDatabase.CONFLICT_REPLACE)
    }
    fun read(owner: String, name: String): String? =
        readableDatabase.rawQuery("SELECT body FROM cache WHERE owner=? AND name=?", arrayOf(owner,name))
            .use { if (it.moveToFirst()) it.getString(0) else null }
    fun enqueue(owner: String, body: JSONObject) {
        writableDatabase.insertOrThrow("outbox", null, ContentValues().apply {
            put("id", body.getString("id")); put("owner", owner); put("body", body.toString())
        })
    }
    fun pending(owner: String): List<JSONObject> =
        readableDatabase.rawQuery("SELECT body FROM outbox WHERE owner=?", arrayOf(owner)).use { cursor ->
            buildList { while (cursor.moveToNext()) add(JSONObject(cursor.getString(0))) }
        }
    fun acknowledge(id: String, owner: String) {
        writableDatabase.delete("outbox", "id=? AND owner=?", arrayOf(id,owner))
    }
}

// Keystore-backed AES-GCM keeps refresh tokens out of plaintext preferences/backups.
class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey("prasa.session", null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder("prasa.session", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    fun save(value: JSONObject) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        val data = cipher.iv + cipher.doFinal(value.toString().toByteArray(Charsets.UTF_8))
        prefs.edit().putString("data", Base64.encodeToString(data, Base64.NO_WRAP)).commit()
    }
    fun load(): JSONObject? = runCatching {
        val data = Base64.decode(prefs.getString("data", null) ?: return null, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, data.copyOfRange(0,12)))
        }
        JSONObject(String(cipher.doFinal(data.copyOfRange(12,data.size)), Charsets.UTF_8))
    }.getOrNull()
    fun clear() { prefs.edit().clear().commit() }
}
