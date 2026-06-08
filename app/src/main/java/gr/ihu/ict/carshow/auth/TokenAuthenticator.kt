package gr.ihu.ict.carshow.auth

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Route



// Triggered by OkHttp only when server returns HTTP 401 Unauthorized status code
// Indicates current access token has expired
class TokenAuthenticator(private val context: Context) : okhttp3.Authenticator {

    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {

        // Get the stored Refresh Token
        val refreshToken = TokenStore.getRefresh(context) ?: return null

        // Synchronously request a new Access Token using the Refresh Token
        val newTokenResponse = refreshAccessToken(refreshToken)

        return if (newTokenResponse != null) {
            // Save the new tokens (use the old refresh if the server didn't send a new one)
            val newAccess = newTokenResponse.access
            val newRefresh = newTokenResponse.refresh ?: refreshToken

            TokenStore.saveTokens(context, newAccess, newRefresh)

            // Retry the failed request with the new Access Token
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccess")
                .build()
        } else {
            // Refresh failed. Clear data and force logout
            TokenStore.clear(context)

            // Broadcast an intent to notify the application that the token session has expired
            val intent = Intent("gr.ihu.ict.carshow.TOKEN_EXPIRED").apply {
                setPackage(context.packageName)
            }
            context.sendBroadcast(intent)

            null
        }
    }


    // Executes synchronous POST request to authentication server
    // A valid Refresh Token for a new Access Token
    private fun refreshAccessToken(refreshToken: String): TokenResponse? {
        // Use fresh OkHttpClient to avoid interceptor infinite loops of 401 errors
        val client = OkHttpClient()

        // Create the request data body
        // Using the key "refresh" with the requirement to use Django SimpleJWT
        // on the backend server (that's what the server expects to receive)
        val body = FormBody.Builder()
            .add("refresh", refreshToken)
            .build()

        // Url string has the actual backend configuration endpoint of server
        val request = Request.Builder()
            .url("http://192.168.1.162:8000/api/token/refresh/")
            .post(body)
            .build()


        return try {
            // execute() makes sure this network call will run synchronously
            // Authenticator needs to hold the main request until token status resolved
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string()
                // Deserialize the JSON response into TokenResponse data class
                Gson().fromJson(json, TokenResponse::class.java)
            } else {
                null // Server rejected the Refresh Token (e.g. Token Expired)
            }
        } catch (e: Exception) {
            null // Network timeout (server down)
        }
    }
}