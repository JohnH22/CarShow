package gr.ihu.ict.carshow.data.model


// Data model used to send a new review to the server
// It contains only the fields required by the API for creation
data class ReviewRequest(
    // The star rating given by the user for the vehicle
    val rating: Float,
    // The text feedback or comments about the vehicle (Review text)
    val comment: String
)
