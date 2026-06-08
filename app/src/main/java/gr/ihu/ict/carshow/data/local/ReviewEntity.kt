package gr.ihu.ict.carshow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


// Represents a vehicle review inside the local Room database table
@Entity(tableName = "vehicle_reviews")
data class ReviewEntity(
    // Unique ID for the local database row
    @PrimaryKey
    val id: Int,
    // Links this review to a specific car entry (Foreign key logic)
    val vehicleId: Int,
    // The name of the user who wrote the review
    val username: String,
    // The star rating given by the user (e.g. 4.5)
    val rating: Float,
    // The actual text feedback from the user (Review text)
    val comment: String,
    // The date and the time when the review was created
    val createdAt: String
)
