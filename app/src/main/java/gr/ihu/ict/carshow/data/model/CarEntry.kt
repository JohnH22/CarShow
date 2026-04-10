package gr.ihu.ict.carshow.data.model

import android.graphics.Bitmap

data class CarEntry(
    val id: Int,
    val brand: String,
    val model: String,
    val category: CarCategory,
    val year: Int,
    val price: Double,
    val priceNegotiable: Boolean,
    val mainImage: Bitmap,
    val description: String,
    val engine: String,
    val horsepower: Int,
    val drivetrain: String,
    val transmission: String,
    val torque: Int,
    val consumption: String,
    val mileage: Int,
    val interiorColor: String,
    val exteriorColor: String,
    val wheelSize: Int,
    val doors: Int,
    val passengers: Int,
    val isRightHandDrive: Boolean,
    val location: String,
    val sellerType: SellerType,
    val rating: Float,
    val videoUrl: String? = null
)
