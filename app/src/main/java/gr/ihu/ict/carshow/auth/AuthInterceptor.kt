package gr.ihu.ict.carshow.auth

import android.content.Context
import gr.ihu.ict.carshow.data.rest.TokenExpiredException
import okhttp3.Interceptor
import okhttp3.Response

// AuthInterceptor injects the Access Token into every outgoing request
// Monitors the responses for permanent authentication failures
class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        // Retrieve the current Access Token from storage
        val token = TokenStore.getAccess(context)

        // If token exists clone the request and add Authorization header
        // Using "Bearer" required by OAuth2/JWT implementation
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            // If no token is found (first time login) , proceed with original request
            chain.request()
        }

        // Proceed with the network request and wait for server response
        val response = chain.proceed(request)

        // Handling 401 unauthorized scenario
        // If code reaches here means TokenAuthenticator failed to refresh the token or session invalid
        if (response.code == 401) {
            // Clear all stored tokens to clean the app from corrupted/expired data
            TokenStore.clear(context)

            // Closing response body to release system resources and prevent memory leaks
            response.close()

            // Throw custom Exception to notify ViewModel that user needs to re-authenticate
            throw TokenExpiredException()
        }

        // Return the successful response
        return response
    }
}