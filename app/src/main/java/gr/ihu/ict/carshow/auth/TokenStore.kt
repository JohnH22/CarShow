package gr.ihu.ict.carshow.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenStore {
    private const val PREFS_FILE = "secure_prefs"
    private const val KEY_TOKEN = "auth_token"

    private fun prefs(context: Context): SharedPreferences =
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM

        )


    fun save(context: Context, token: String) =
        prefs(context).edit { putString(KEY_TOKEN, token) }


    fun get(context: Context): String? =
        prefs(context).getString(KEY_TOKEN, null)


    fun clear(context: Context) =
        prefs(context).edit { remove(KEY_TOKEN) }
}