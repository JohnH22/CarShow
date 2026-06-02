package gr.ihu.ict.carshow.data.model


// App domain model representing a vehicle review
// This clean data structure is used directly by Compose UI to display reviews
data class VehicleReview(
    // The unique ID of the review from the database server
    val id: Int = 0,
    // The username of the user who wrote the review
    val username: String,
    // The star rating the user given to the vehicle
    val rating: Float,
    // The actual text message from the user (Review text)
    val comment: String,
    // The time and date showing when the review was created
    val createdAt: String
)
