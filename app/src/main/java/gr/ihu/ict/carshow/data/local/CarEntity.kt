package gr.ihu.ict.carshow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "car_entries")
data class CarEntity(
    // Room gives automatically IDs(1,2,3...)
    @PrimaryKey(autoGenerate = true)
    // 0 means Room will auto set the ID
    val id: Int = 0,

    val brand: String,
    val model: String,
    val category: String,
    val year: Int,
    val price: Double,
    val priceNegotiable: Boolean,
    val mainImageData: ByteArray,
    val description: String,
    val engine: String,
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
    val rating: Double,
    val videoUrl: String? = null

)
