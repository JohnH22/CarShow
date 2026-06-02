package gr.ihu.ict.carshow.data.rest


import gr.ihu.ict.carshow.auth.LoginRequest
import gr.ihu.ict.carshow.auth.LoginResponse
import gr.ihu.ict.carshow.auth.RegisterRequest
import gr.ihu.ict.carshow.auth.RegisterResponse
import gr.ihu.ict.carshow.auth.TokenResponse
import gr.ihu.ict.carshow.data.model.ReviewRequest
import gr.ihu.ict.carshow.data.model.VehicleReview
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CarEntryApiService {

    @GET("car_entry/")
    suspend fun getAllCarEntries(
        @Query("category") category: String? = null,
        @Query("price__gte") minPrice: Double? = null,
        @Query("price__lte") maxPrice: Double? = null,
        @Query("engine__gte") minEngine: Int? = null,
        @Query("engine__lte") maxEngine: Int? = null,
        @Query("mileage__gte") minMileage: Int? =null,
        @Query("mileage__lte") maxMileage: Int? =null,
        @Query("horsepower__gte") minHP: Int? =null,
        @Query("horsepower__lte") maxHP: Int? =null,
        @Query("year__gte") minYear: Int? =null,
        @Query("year__lte") maxYear: Int? =null,
        @Query("ordering") ordering: String? = null
    ): List<CarEntryDto>

    @POST("car_entry/")
    suspend fun addCarEntry(@Body carEntryDto: CarEntryDto): CarEntryDto

    @GET("car_entry/{id}/")
    suspend fun getById(@Path("id") id: Int): CarEntryDto

    @GET("car_entry/byCategory/{category}/")
    suspend fun getByCategory(@Path("category") category: String): List<CarEntryDto>

    @DELETE("car_entry/{id}/")
    suspend fun deleteCarEntry(@Path("id") id: Int)

    @POST("login/")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/register/")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("api/token/refresh/")
    suspend fun refreshToken(@Body body: Map<String, String>): TokenResponse

    @GET("car_entry/{id}/reviews")
    suspend fun getVehicleReviews(@Path("id") id: Int): List<VehicleReview>

    @POST("car_entry/{id}/reviews/")
    suspend fun postReview(@Path("id") id: Int, @Body request: ReviewRequest): VehicleReview
}