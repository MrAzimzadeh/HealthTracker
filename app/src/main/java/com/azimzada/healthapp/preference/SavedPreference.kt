package com.azimzada.healthapp.preference
import android.content.Context
import android.content.SharedPreferences

object SavedPreference {

    private const val PREFERENCE_NAME = "MyAppPrefs"

    // Anahtar-değer çiftleri için anahtar adları
    private const val KEY_EMAIL = "email"
    private const val KEY_USERNAME = "username"


    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    fun setEmail(context: Context, email: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(KEY_EMAIL, email)
        editor.apply()
    }


    fun getEmail(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_EMAIL, null)
    }

    // Kullanıcı adı değerini kaydet
    fun setUsername(context: Context, username: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(KEY_USERNAME, username)
        editor.apply()
    }

    // Kullanıcı adı değerini al
    fun getUsername(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USERNAME, null)
    }
}