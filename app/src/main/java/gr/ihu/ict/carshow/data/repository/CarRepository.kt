package gr.ihu.ict.carshow.data.repository

import gr.ihu.ict.carshow.data.model.CarEntry
import gr.ihu.ict.carshow.data.model.CarCategory
import gr.ihu.ict.carshow.data.model.ReviewRequest
import gr.ihu.ict.carshow.data.model.VehicleReview
import gr.ihu.ict.carshow.ui.viewmodel.CarFilterState
import kotlinx.coroutines.flow.Flow

// Interface defining the data operations for the application
// It acts as a bridge between the ViewModels and the data sources (API & Room)
interface CarRepository {
    // Provides continuous stream of all car entries from the local database
    fun getCarsStream(category: String?, orderBy: String?): Flow<List<CarEntry>>

    // Provides a reactive stream of reviews for a specific vehicle
    // This allows the UI to update automatically when new reviews are synced or added
    fun getReviewsStream(vehicleId: Int): Flow<List<VehicleReview>>

    // Synchronizes the local database with the remote API data
    suspend fun refreshCars(filters: CarFilterState)

    // Retrieves a specific car ID checking the local storage first
    suspend fun getCarById(id: Int): CarEntry?

    // Submits a new vehicle entry to the server and saves it locally
    suspend fun addCarEntry(carEntry: CarEntry)

    // Removes a vehicle entry from both the remote server and the local database
    suspend fun deleteCar(carEntry: CarEntry)

    // Fetches the latest reviews for a vehicle from the API and updates the local database
    // Returns the list of reviews
    suspend fun getVehicleReviews(id: Int): List<VehicleReview>

    // Posts a new review for a vehicle and stores the response in the local database
    suspend fun postReview(request: ReviewRequest): VehicleReview

    // Removes a specific review from both the remote server and the local database
    suspend fun deleteReview(reviewId: Int)
}