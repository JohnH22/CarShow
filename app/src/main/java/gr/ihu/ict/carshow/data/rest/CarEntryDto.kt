package gr.ihu.ict.carshow.data.rest



data class CarImageDto(
    val imageUrl: String
)


data class CarEntryDto(
    val id: Int,
    val brand: String? = null,
    val model: String? = null,
    val category: String? = null,
    val year: Int? = null,
    val price: Double? = null,
    val priceNegotiable: Boolean? = null,
    val images: List<CarImageDto> = emptyList(),
    val description: String? = null,
    val engine: Int? = null,
    val fuelType: String? = null,
    val horsepower: Int? = null,
    val drivetrain: String? = null,
    val transmission: String? = null,
    val torque: Int? = null,
    val consumption: Double? = null,
    val mileage: Int? = null,
    val interiorColor: String? = null,
    val exteriorColor: String? = null,
    val wheelSize: Int? = null,
    val doors: Int? = null,
    val passengers: Int? = null,
    val isRightHandDrive: Boolean? = null,
    val location: String? = null,
    val sellerType: String? = null,
    val rating: Float? = null,
    val videoUrl: String? = null
)
