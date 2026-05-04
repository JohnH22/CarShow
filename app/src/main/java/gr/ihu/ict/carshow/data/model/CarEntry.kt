package gr.ihu.ict.carshow.data.model

import android.graphics.Bitmap

data class CarEntry(
    val id: Int =0,
    val brand: String,
    val model: String,
    val category: CarCategory,
    val year: Int,
    val price: Double,
    val priceNegotiable: Boolean = false,
    val mainImage: Bitmap,
    val description: String = "No description provided.",
    val engine: String = "N/A",
    val horsepower: Int = 0,
    val drivetrain: String = "FWD",
    val transmission: String = "Manual",
    val torque: Int = 0,
    val consumption: String = "0.0 l/100km",
    val mileage: Int = 0,
    val interiorColor: String = "Black",
    val exteriorColor: String = "White",
    val wheelSize: Int = 17,
    val doors: Int = 4,
    val passengers: Int = 5,
    val isRightHandDrive: Boolean = false,
    val location: String = "Unknown",
    val sellerType: SellerType = SellerType.PRIVATE,
    val rating: Float = 5.0f,
    val videoUrl: String? = null
)
