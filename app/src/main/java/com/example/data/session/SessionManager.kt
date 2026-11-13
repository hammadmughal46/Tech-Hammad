package com.example.data.session

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pak_ads_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "logged_in_user_id"
        private const val KEY_ADMIN_LOGGED_IN = "admin_logged_in"
        private const val ADMIN_PASSCODE = "085213"

        fun hashPassword(password: String): String {
            val salt = "PAK_ADS_SALT_2026_SECURE"
            val bytes = (password + salt).toByteArray(Charsets.UTF_8)
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            return digest.joinToString("") { "%02x".format(it) }
        }

        fun verifyAdminPassword(input: String): Boolean {
            return input.trim() == ADMIN_PASSCODE
        }
    }

    fun saveUserId(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1L)
    }

    fun setAdminLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_ADMIN_LOGGED_IN, loggedIn).apply()
    }

    fun isAdminLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_ADMIN_LOGGED_IN, false)
    }

    fun clearSession() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    fun clearAdminSession() {
        prefs.edit().remove(KEY_ADMIN_LOGGED_IN).apply()
    }
}
