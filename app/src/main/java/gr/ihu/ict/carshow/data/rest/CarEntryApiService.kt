package gr.ihu.ict.carshow.data.rest


import gr.ihu.ict.carshow.auth.LoginRequest
import gr.ihu.ict.carshow.auth.LoginResponse
import gr.ihu.ict.carshow.auth.RegisterRequest
import gr.ihu.ict.carshow.auth.RegisterResponse
import gr.ihu.ict.carshow.auth.TokenResponse
import gr.ihu.ict.carshow.data.model.ReviewRequest
import gr.ihu.ict.carshow.data.model.VehicleReview
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query



// Retrofit interface defining all remote HTTP operations and API network endpoints
interface CarEntryApiService {


    // Fetches a filtered list of vehicles using icontains, gte (>=), and lte (<=) query parameters mapped by Django
    @GET("api/vehicles/")
    suspend fun getAllCarEntries(
        @Query("brand__icontains") brand: String? = null,
        @Query("model_name__icontains") model: String? = null,
        @Query("location__icontains") location: String? = null,
        @Query("category__icontains") category: String? = null,
        @Query("fuel_type__icontains") fuelType: String? = null,
        @Query("drivetrain__icontains") drivetrain: String? = null,
        @Query("transmission__icontains") transmission: String? = null,
        @Query("interior_color__icontains") interiorColor: String? = null,
        @Query("exterior_color__icontains") exteriorColor: String? = null,
        @Query("seller_type__icontains") sellerType: String? = null,
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
        @Query("consumption__gte") minConsumption: Double? = null,
        @Query("consumption__lte") maxConsumption: Double? = null,
        @Query("ordering") ordering: String? = null
    ): List<CarEntryDto>

    // Submits a new vehicle entry to the backend server
    @POST("api/vehicles/")
    suspend fun addCarEntry(@Body carEntryDto: CarEntryDto): CarEntryDto

    // Retrieves details for a specific vehicle using its unique ID
    @GET("api/vehicles/{id}/")
    suspend fun getById(@Path("id") id: Int): CarEntryDto

    // Permanently deletes a vehicle entry from the remote server by ID
    @DELETE("api/vehicles/{id}/")
    suspend fun deleteCarEntry(@Path("id") id: Int)

    // Submits user credentials to obtain authentication session tokens
    @POST("api/token/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Registers a new user account on the backend system
    @POST("api/register/")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    // Requests a new access token using a valid refresh token
    @POST("api/token/refresh/")
    suspend fun refreshToken(@Body body: Map<String, String>): TokenResponse

    // Fetches all user reviews associated with a specific vehicle ID
    @GET("api/reviews/")
    suspend fun getVehicleReviews(@Query("vehicle") vehicleId: Int): List<VehicleReview>

    // Submits a new user review for a vehicle
    @POST("api/reviews/")
    suspend fun postReview(@Body request: ReviewRequest): Response<VehicleReview>

    // Deletes a specific review by its unique path ID
    @DELETE("api/reviews/{id}/")
    suspend fun deleteReview(@Path("id") reviewId: Int): Response<Unit>
}