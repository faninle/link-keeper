package com.linkkeeper.app

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class LinkItem(
    val id: String,
    val group: String,
    val title: String,
    val url: String,
    val description: String,
)

/** Stores links and groups as JSON in the app's private storage. */
class LinkStore(context: Context) {
    private val prefs = context.getSharedPreferences("links", Context.MODE_PRIVATE)

    fun loadLinks(): List<LinkItem> {
        val raw = prefs.getString("links", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                LinkItem(
                    o.getString("id"),
                    o.getString("group"),
                    o.getString("title"),
                    o.getString("url"),
                    o.optString("description", ""),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadGroups(): List<String> {
        val raw = prefs.getString("groups", null) ?: return listOf("عمومی")
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            listOf("عمومی")
        }
    }

    fun save(links: List<LinkItem>, groups: List<String>) {
        val la = JSONArray()
        links.forEach {
            la.put(
                JSONObject()
                    .put("id", it.id)
                    .put("group", it.group)
                    .put("title", it.title)
                    .put("url", it.url)
                    .put("description", it.description)
            )
        }
        val ga = JSONArray()
        groups.forEach { ga.put(it) }
        prefs.edit().putString("links", la.toString()).putString("groups", ga.toString()).apply()
    }
}

/** Keeps only a salted PBKDF2 hash of the password, never the password itself. */
class PasswordStore(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun isSet(): Boolean = prefs.contains("hash") && prefs.contains("salt")

    fun set(password: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = derive(password, salt)
        prefs.edit()
            .putString("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString("hash", Base64.encodeToString(hash, Base64.NO_WRAP))
            .apply()
    }

    fun verify(password: String): Boolean {
        val salt = prefs.getString("salt", null) ?: return false
        val hash = prefs.getString("hash", null) ?: return false
        val actual = derive(password, Base64.decode(salt, Base64.NO_WRAP))
        return MessageDigest.isEqual(actual, Base64.decode(hash, Base64.NO_WRAP))
    }

    private fun derive(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }
}
