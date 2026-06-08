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

    // The backend deployment server base URL address
    private const val BASE_URL = "http://192.168.1.162:8000/"

    // Volatile reference to ensure immediate visibility of the API service instance across threads
    @Volatile
    private var apiService: CarEntryApiService? = null

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
        return apiService ?: synchronized(this) {
            apiService ?: run {

                val appContext = context.applicationContext

                // Configure HTTP client with Authentication logic
                val client = OkHttpClient.Builder()
                    // The Interceptor adds the "Authorization: Bearer <token>" header to every request
                    .addInterceptor (AuthInterceptor(appContext))
                    // Authenticator handles auto token refresh when a 401 error occurs
                    .authenticator(TokenAuthenticator(appContext))
                    .build()

                // Initializing Retrofit with the custom OkHttp client and Gson converter
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

                val service = retrofit.create(CarEntryApiService::class.java)
                apiService = service
                service
            }
        }
    }
}