package gr.ihu.ict.carshow.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey


// TokenStore is a secure singleton object responsible for "holding" the authentication tokens
// Using hardware-backed encryption
object TokenStore {
    private const val PREFS_FILE = "secure_prefs"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"


    // Uses AES-256 encryption for both keys and values
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


    // Saves both Access and Refresh Tokens
    fun saveTokens(context: Context, access: String, refresh: String) =
        prefs(context).edit {
            putString(KEY_ACCESS, access)
            putString(KEY_REFRESH, refresh)
        }


    // Retrieves the encrypted Access Token. Returns null if not found
    fun getAccess(context: Context): String? =
        prefs(context).getString(KEY_ACCESS, null)


    // Retrieves the encrypted Refresh Token. Returns null if not found
    fun getRefresh(context: Context): String? =
        prefs(context).getString(KEY_REFRESH, null)


    // Clears all authentication data from device
    // Called during Logout or when session permanently expired
    fun clear(context: Context) =
        prefs(context).edit {
            remove(KEY_ACCESS)
            remove(KEY_REFRESH)
        }
}