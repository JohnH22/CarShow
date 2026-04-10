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

object RetrofitInstance {

    private const val BASE_URL = ""

    private val gson = GsonBuilder()
        .registerTypeAdapter(ByteArray::class.java, object : JsonDeserializer<ByteArray> {
            override fun deserialize(
                json: JsonElement,
                typeOfT: Type,
                context: JsonDeserializationContext
            ): ByteArray {
                return Base64.decode(json.asString, Base64.DEFAULT)
            }
        })
        .create()

    fun buildApi(context: Context): CarEntryApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor (AuthInterceptor(context.applicationContext))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CarEntryApiService::class.java)
    }
}