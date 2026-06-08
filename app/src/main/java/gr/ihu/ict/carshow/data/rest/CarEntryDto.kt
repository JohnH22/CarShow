package gr.ihu.ict.carshow.data.rest

import com.google.gson.annotations.SerializedName


// Helper class to read each image URL from the API's JSON list
data class CarImageDto(
    @SerializedName("image_url")
    val imageUrl: String
)


// Data Transfer Object used to deserialize the core vehicle JSON responses from the remote API.
// Uses SerializedName annotations to map snake_case JSON keys from Django to camelCase Kotlin variables.
data class CarEntryDto(
    val id: Int,
    val brand: String? = null,

    @SerializedName("model_name")
    val model: String? = null,

    val category: String? = null,
    val year: Int? = null,
    val price: Double? = null,

    @SerializedName("price_negotiable")
    val priceNegotiable: Boolean? = null,

    @SerializedName("images")
    val images: List<CarImageDto>? = emptyList(),

    val description: String? = null,
    val engine: Int? = null,

    @SerializedName("fuel_type")
    val fuelType: String? = null,

    val horsepower: Int? = null,
    val drivetrain: String? = null,
    val transmission: String? = null,
    val torque: Int? = null,
    val consumption: Double? = null,
    val mileage: Int? = null,

    @SerializedName("interior_color")
    val interiorColor: String? = null,

    @SerializedName("exterior_color")
    val exteriorColor: String? = null,

    @SerializedName("wheel_size")
    val wheelSize: Int? = null,

    val doors: Int? = null,
    val passengers: Int? = null,

    @SerializedName("is_right_hand_drive")
    val isRightHandDrive: Boolean? = null,

    val location: String? = null,

    @SerializedName("seller_type")
    val sellerType: String? = null,
    val rating: Float? = null,

    @SerializedName("average_rating")
    val averageRating: Double? = 0.0,

    @SerializedName("video_url")
    val videoUrl: String? = null
)
