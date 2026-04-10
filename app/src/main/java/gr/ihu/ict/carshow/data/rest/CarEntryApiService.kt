package gr.ihu.ict.carshow.data.rest


import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CarEntryApiService {

    @GET("car_entry")
    suspend fun getAllCarEntries(): List<CarEntryDto>

    @POST("car_entry")
    suspend fun addCarEntry(@Body carEntryDto: CarEntryDto): CarEntryDto

    @GET("car_entry/{id}")
    suspend fun getById(@Path("id") id: Int): CarEntryDto

    @GET("car_entry/byCategory/{category}")
    suspend fun getByCategory(@Path("category") category: String): List<CarEntryDto>

//    @POST("/login")
//    suspend fun login(@Body request: LoginRequest): LoginResponse
}