package gr.ihu.ict.carshow.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.ihu.ict.carshow.data.model.CarEntry
import gr.ihu.ict.carshow.data.model.ReviewRequest
import gr.ihu.ict.carshow.data.model.VehicleReview
import gr.ihu.ict.carshow.data.repository.CarRepository
import gr.ihu.ict.carshow.data.rest.TokenExpiredException
import kotlinx.coroutines.launch

class CarDetailViewModel(
    private val repository: CarRepository
) : ViewModel() {


    // CarEntry state: Holds the details of the currently selected vehicle
    //Compose state mutableStateOf used for simple state observation
    //Private set ensures can be only modified in this ViewModel
    var car by mutableStateOf<CarEntry?>(null)
        private set

    // Global error message state to notify the user about the network or database issues
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Loading state flag to toggle progress indicator in the UI
    var isLoading by mutableStateOf(false)
        private set

    // Reactive list of reviews, this is updated automatically via the observeReviews Flow
    var reviews by mutableStateOf<List<VehicleReview>>(emptyList())
        private set

    // Success message state for temporary feedback (e.g., after successful review submit)
    var successMessage by mutableStateOf<String?>(null)
        private set

    var isDeleteSuccess by mutableStateOf(false)
        private set




    // Fetches a specific car by ID
    // The repository checks the local Room database first for instant display
    fun getCar(id: Int, onTokenExpired: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                //Checking first on local Room DB if it exists
                val result = repository.getCarById(id)

                if (result == null) {
                    errorMessage = "The vehicle was not found or is unavailable."
                } else {
                    car = result
                }
            } catch (e: TokenExpiredException) {
                onTokenExpired()
            } catch (e: Exception) {
                errorMessage = "Failed to load vehicle details. Please check your connection."
            } finally {
                isLoading = false
            }
        }
    }


    // Reactive observation of the local database reviews
    // Collecting the Flow from Room so the UI updates automatically whenever
    // A new review is added or refreshed from the server
    fun observerReviews(vehicleId: Int) {
        viewModelScope.launch {
            repository.getReviewsStream(vehicleId).collect { listFromRoom ->
                reviews = listFromRoom
            }
        }
    }


    // Triggers a network request to refresh reviews from the API
    // The repository will save the results to Room, which then updates "reviews" via the Flow
    fun fetchReviews(vehicleId: Int) {
        viewModelScope.launch {
            isLoading = true

            try {
                repository.getVehicleReviews(vehicleId)
            } catch (e: Exception) {
                // If API fails we inform the user that they are viewing cached data
                errorMessage = "Offline mode: Showing cached reviews."
            } finally {
                isLoading = false
            }
        }
    }




    // Sends a new review to the server and local database
    // Don't need to manually append because observing the database Flow does it automatically
    // The new review list gets appended auto and the Room handles the update notification
    fun addReview(vehicleId: Int, rating: Float, comment: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                repository.postReview(vehicleId, ReviewRequest(rating, comment))

                successMessage = "Review submitted successfully!"
            } catch (e: Exception) {
                errorMessage = "Failed to submit review."
            } finally {
                isLoading = false
            }
        }
    }


    fun deleteVehicle(carEntry: CarEntry, onTokenExpired: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                repository.deleteCar(carEntry)
                successMessage = "Vehicle deleted successfully!"
                isDeleteSuccess = true
            } catch (e: TokenExpiredException) {
                onTokenExpired()
            } catch (e: Exception) {
                errorMessage = "Failed to delete the vehicle. Please check your connection."
            } finally {
                isLoading = false
            }
        }
    }



    //Resetting the state so on navigation next car doesn't show even briefly old data
    fun clearState() {
        car = null
        reviews = emptyList()
        errorMessage = null
        isDeleteSuccess = false
    }

    // Clears the messages to prevent them from reappearing during recomposition
    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    // Resets the delete flag after the UI has successfully reacted to it
    fun resetDeleteFlag() {
        isDeleteSuccess = false
    }
}