package gr.ihu.ict.carshow.auth

import android.content.Context
import gr.ihu.ict.carshow.data.rest.TokenExpiredException
import okhttp3.Interceptor
import okhttp3.Response

// AuthInterceptor injects the Access Token into every outgoing request
// Monitors the responses for permanent authentication failures
class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val appContext = context.applicationContext

        // Retrieve the current Access Token from storage
        val token = TokenStore.getAccess(appContext)

        // If token exists clone the request and add Authorization header
        // Using "Bearer" required by OAuth2/JWT implementation
        val request = if (token != null) {
            android.util.Log.d("AUTH_DEBUG", "Sending Token: Bearer $token")
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token") // header instead of addHeader to override any old tokens
                .build()
        } else {
            android.util.Log.d("AUTH_DEBUG", "No token found in TokenStore!")
            // If no token is found (first time login) , proceed with original request
            chain.request()
        }

        // Proceed with the network request and wait for server response
        val response = chain.proceed(request)

        // Handling 401 unauthorized scenario
        // If code reaches here means TokenAuthenticator failed to refresh the token or session invalid
        if (response.code == 401) {

            // Check if the request has already been retried once
            val isPermanent401 = response.priorResponse != null
            val hadNoToken = response.request.header("Authorization") == null

            // If the failure is permanent after a retry attempt, clear the session and throw exception
            if (isPermanent401 && hadNoToken) {
                TokenStore.clear(appContext)
                response.close()
                throw TokenExpiredException()
            }
        }


        // Return the successful response
        return response
    }
}