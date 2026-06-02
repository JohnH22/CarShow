package gr.ihu.ict.carshow.data.model

import android.graphics.Bitmap

data class CarEntry(
    val id: Int =0,
    val brand: String,
    val model: String, // Matches model_name in Django backend
    val category: CarCategory,
    val year: Int,
    val price: Double,
    val priceNegotiable: Boolean = false,
    val imageUrls: List<String> = emptyList(),
    val description: String = "No description provided.",
    val engine: Int = 0, // Changed from String to Int
    val fuelType: String,
    val horsepower: Int = 0,
    val drivetrain: String = "FWD",
    val transmission: String = "Manual",
    val torque: Int = 0,
    val consumption: Double = 0.0, // Changed from String to Double (e.g., 5.4)
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
