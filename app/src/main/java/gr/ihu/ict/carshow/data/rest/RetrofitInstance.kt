package gr.ihu.ict.carshow.data.rest

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import android.util.Base64
import gr.ihu.ict.carshow.auth.AuthInterceptor
import gr.ihu.ict.carshow.auth.TokenAuthenticator


// Singleton object responsible for configuring and providing the Retrofit API service
object RetrofitInstance {

    // Server's IP goes here
    private const val BASE_URL = ""

    // Custom Gson instance configured to auto decode Base64 strings into
    // ByteArrays during JSON deserialization
    private val gson = GsonBuilder()
        .registerTypeAdapter(ByteArray::class.java, object : JsonDeserializer<ByteArray> {
            override fun deserialize(
                json: JsonElement,
                typeOfT: Type,
                context: JsonDeserializationContext
            ): ByteArray {
                // Decodes the Base64 image string coming from API into a usable ByteArray
                return Base64.decode(json.asString, Base64.DEFAULT)
            }
        })
        .create()

    // Build and configure API service
    fun buildApi(context: Context): CarEntryApiService {
        // Configure HTTP client with Authentication logic
        val client = OkHttpClient.Builder()
            // The Interceptor adds the "Authorization: Bearer <token>" header to every request
            .addInterceptor (AuthInterceptor(context.applicationContext))
            // Authenticator handles auto token refresh when a 401 error occurs
            .authenticator(TokenAuthenticator(context.applicationContext))
            .build()

        // Initializing Retrofit with the custom OkHttp client and Gson converter
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CarEntryApiService::class.java)
    }
}