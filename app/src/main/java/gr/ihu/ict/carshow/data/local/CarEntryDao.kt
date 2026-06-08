package gr.ihu.ict.carshow.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CarEntryDao {

    // Retrieves an observable stream of vehicle entries filtered and ordered dynamically based on user criteria
    @Query("""
        SELECT * FROM car_entries 
        ORDER BY 
            CASE WHEN :orderBy = 'brand' THEN brand END ASC,
            CASE WHEN :orderBy = 'price' THEN price END ASC,
            CASE WHEN :orderBy = '-price' THEN price END DESC,
            CASE WHEN :orderBy = 'year' THEN year END ASC,
            CASE WHEN :orderBy = '-year' THEN year END DESC,
            CASE WHEN :orderBy = 'engine' THEN engine END ASC,
            CASE WHEN :orderBy = '-engine' THEN engine END DESC,
            CASE WHEN :orderBy = '-average_rating' THEN averageRating END DESC,
            id DESC
    """)
    fun getFilteredCarsStream(orderBy: String?): Flow<List<CarEntity>>


    // Fetches a single car entry by its unique ID
    // Marked as suspend to ensure it runs on a background thread
    @Query("SELECT * FROM car_entries WHERE id = :id")
    suspend fun getCarById(id: Int): CarEntity?

    // Inserts or updates a single car entry
    // If the ID already exists, it replaces the old data (OnConflictStrategy.REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCar(car: CarEntity)

    // Insertion of car entries, typically used when refreshing data from the API
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCars(cars: List<CarEntity>)

    // Deletes a car entry from the local database based on its ID
    @Query("DELETE FROM car_entries WHERE id = :id")
    suspend fun deleteCarById(id: Int)

    // Deletes a specific car entity object from the database
    @Delete
    suspend fun deleteCar(car: CarEntity)

    // Wipes all car entries from the database
    // Useful for data reset or user logout scenarios
    @Query("DELETE FROM car_entries")
    suspend fun deleteAll()

    // Retrieves all reviews with a specific vehicle ID ordered by the most recent creation date
    @Query("SELECT * FROM vehicle_reviews WHERE vehicleId = :vehicleId ORDER BY createdAt DESC")
    fun getReviewsForVehicle(vehicleId: Int): Flow<List<ReviewEntity>>

    // Inserts multiple review entities
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewEntity>)

    // Inserts a single review. Used when a user submits a new review locally
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleReview(review: ReviewEntity)

    // Removes all reviews related to a specific vehicle ID
    // Often used during a full refresh or when a vehicle is deleted
    @Query("DELETE FROM vehicle_reviews WHERE vehicleId = :vehicleId")
    suspend fun deleteReviewsForVehicle(vehicleId: Int)


    // Deletes a single specific review by its unique ID from the local database
    @Query("DELETE FROM vehicle_reviews WHERE id = :reviewId")
    suspend fun deleteReviewById(reviewId: Int)
}