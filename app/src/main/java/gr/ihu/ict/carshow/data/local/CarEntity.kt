package gr.ihu.ict.carshow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName


// Defines the Room database table structure for storing vehicle details locally
@Entity(tableName = "car_entries")
data class CarEntity(

    @PrimaryKey
    val id: Int,

    val brand: String,
    val model: String,
    val category: String,
    val year: Int,
    val price: Double,
    val priceNegotiable: Boolean,

    // Uses custom TypeConverters to serialize/deserialize the List of image URLs into a single database string
    @TypeConverters(Converters::class)
    val imageUrls: List<String>,

    val description: String,
    val engine: Int,
    val fuelType: String,
    val horsepower: Int,
    val drivetrain: String,
    val transmission: String,
    val torque: Int,
    val consumption: Double,
    val mileage: Int,
    val interiorColor: String,
    val exteriorColor: String,
    val wheelSize: Int,
    val doors: Int,
    val passengers: Int,
    val isRightHandDrive: Boolean,
    val location: String,
    val sellerType: String,
    val rating: Float, // Local rating given by the user

    @SerializedName("average_rating")
    val averageRating: Double, // Global average rating from all users

    val videoUrl: String? = null

)
