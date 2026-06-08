package gr.ihu.ict.carshow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.ihu.ict.carshow.data.model.CarEntry
import gr.ihu.ict.carshow.data.repository.CarRepository
import gr.ihu.ict.carshow.data.rest.TokenExpiredException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch




// ViewModel responsible for the Car List screen logic, handling filtered data and synchronization
class CarListViewModel(
    private val repository: CarRepository
) : ViewModel() {


    // Private state holder , _uiState is used only on this class locally as uiState
    private val _uiState = MutableStateFlow(CarListUiState())
    // Public read-only state for the UI compose, used asStateFlow() to prevent UI from modifying it directly
    val uiState: StateFlow<CarListUiState> = _uiState.asStateFlow()

    private var dbObservationJob: Job? = null


    // Runs once when the ViewModel is created to initialize the data stream from Room (data observation and synchronization)
    init {
        setupDatabaseObservation(CarFilterState())
    }


    // Starts an observation stream from the local Room database using active filters
    private fun setupDatabaseObservation(filters: CarFilterState) {
        dbObservationJob?.cancel()

        dbObservationJob = viewModelScope.launch {
            repository.getCarsStream(filters.category, filters.ordering).collect { filteredCars ->
                // Update UI items whenever the local database query results change
                _uiState.update { it.copy(items = filteredCars) }
            }
        }
    }




    // Synchronize local database with the remote server db, applying any active filters or ordering
    fun refreshData(onTokenExpired: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                repository.refreshCars(_uiState.value.filters)

            } catch (e: TokenExpiredException) {
                onTokenExpired()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(errorMessage = "Failed to sync with server.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Updates the active filters in the UI State and automatically triggers a network refresh
     * After saving the new values it auto calls refreshData() to pull the newly filtered car list from Django server
     */
    fun updateFilters(
        newFilters: CarFilterState, // Accepts Django sorting keys (e.g., "price", "-price", "year")
        onTokenExpired: () -> Unit
    ) {
        val cleanedFilters = newFilters.copy(
            brand = newFilters.brand?.trim(),
            model = newFilters.model?.trim(),
            location = newFilters.location?.trim(),
            category = newFilters.category?.trim(),
            fuelType = newFilters.fuelType?.trim(),
            drivetrain = newFilters.drivetrain?.trim(),
            transmission = newFilters.transmission?.trim(),
            interiorColor = newFilters.interiorColor?.trim(),
            exteriorColor = newFilters.exteriorColor?.trim(),
            sellerType = newFilters.sellerType?.trim()
        )

        // Save the new filter choices into the UI State
        _uiState.update {
            it.copy(filters = cleanedFilters)
        }

        // Restart observation with updated parameters
        setupDatabaseObservation(cleanedFilters)

        // Immediately refresh data from server using the newly applied filters
        refreshData(onTokenExpired)
    }

    /**
     * Clears all currently applied filters and sets them back to null
     * Then triggers refreshData() to reload the complete unfiltered
     * car list from the Django database
     */
    fun clearFilters(onTokenExpired: () -> Unit) {

        val emptyFilters = CarFilterState()

        // Reset the filter state back to completely empty
        _uiState.update { it.copy(filters = emptyFilters) }
        // Fetch the original full list of cars from the server

        // Restart observation for the full car list
        setupDatabaseObservation(emptyFilters)

        refreshData(onTokenExpired)
    }


    // Adds new car entry with a callback if needed for redirection on failure
    // Room triggers observeCars() for auto UI update
    fun addCar(car: CarEntry, onTokenExpired: () -> Unit, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            try {
                // Send new car entry to API. If success Room will trigger observeCars() for UI update
                repository.addCarEntry(car)
                _uiState.update { it.copy(successMessage = "The vehicle has been added successfully!!") }
                onSuccess()
            } catch (e: TokenExpiredException) {
                onTokenExpired()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to save vehicle.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }


    // Clears the success banner message from the UI state
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    // Forces a manual loading state for UI operations
    fun setLoading() {
        _uiState.update { it.copy(isLoading = true) }
    }

    // Wipes the displayed car items (useful during logout or navigation transitions)
    fun clearItemsList() {
        _uiState.update { it.copy(items = emptyList()) }
    }
}