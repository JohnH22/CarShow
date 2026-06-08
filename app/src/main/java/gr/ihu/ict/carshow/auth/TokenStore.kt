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
    private const val KEY_USERNAME = "username"



    // Generates or retrieves the master key used to encrypt the shared preferences file
    private fun getMasterKey(context: Context): MasterKey {
        return MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }


    // Initializes and returns the EncryptedSharedPreferences instance
    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        val appContext = context.applicationContext
        return EncryptedSharedPreferences.create(
            appContext, PREFS_FILE, getMasterKey(appContext),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }


    // Saves both Access and Refresh Tokens
    fun saveTokens(context: Context, access: String, refresh: String) {
        getEncryptedPrefs(context).edit {
            putString(KEY_ACCESS, access)
            putString(KEY_REFRESH, refresh)
        }
    }


    // Retrieves the encrypted Access Token. Returns null if not found
    fun getAccess(context: Context): String? =
        getEncryptedPrefs(context).getString(KEY_ACCESS, null)



    // Retrieves the encrypted Refresh Token. Returns null if not found
    fun getRefresh(context: Context): String? =
        getEncryptedPrefs(context).getString(KEY_REFRESH, null)



    // Clears all authentication data from device
    // Called during Logout or when session permanently expired
    fun clear(context: Context) {

        getEncryptedPrefs(context).edit {
            remove(KEY_ACCESS)
            remove(KEY_REFRESH)
            remove(KEY_USERNAME)
        }
    }


    // Saves the currently logged-in username securely
    fun saveUsername(context: Context, username: String) {
        getEncryptedPrefs(context).edit {
            putString(KEY_USERNAME, username)
        }
    }


    // Retrieves the securely stored username. Returns null if not found
    fun getUsername(context: Context): String? =
        getEncryptedPrefs(context).getString(KEY_USERNAME, null)
}