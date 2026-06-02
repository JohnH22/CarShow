package gr.ihu.ict.carshow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.ihu.ict.carshow.data.model.CarEntry
import gr.ihu.ict.carshow.data.repository.CarRepository
import gr.ihu.ict.carshow.data.rest.TokenExpiredException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CarListViewModel(
    private val repository: CarRepository
) : ViewModel() {


    // Private state holder , _uiState is used only on this class locally as uiState
    private val _uiState = MutableStateFlow(CarListUiState())
    // Public read-only state for the UI compose, used asStateFlow() to prevent UI from modifying it directly
    val uiState: StateFlow<CarListUiState> = _uiState.asStateFlow()


    // Runs once when the ViewModel is created (data observation and synchronization)
    init {
        observeCars()
        refreshData()
    }


    // Listens to the Room database for real-time updates
    private fun observeCars() {
        viewModelScope.launch {
            repository.getCarsStream().collect { cars ->
                _uiState.update { it.copy(items = cars) }
            }
        }
    }


    // Synchronize local database with the remote server db, applying any active filters or ordering
    fun refreshData(onTokenExpired: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Extract current active filters from the UI state
                val currentFilters = _uiState.value.filters

                // Repository using refreshCars() to fetch filtered data from API and saves it to Room
                repository.refreshCars(
                    category = null,
                    minPrice = currentFilters.minPrice,
                    maxPrice = currentFilters.maxPrice,
                    minEngine = currentFilters.minEngine,
                    maxEngine = currentFilters.maxEngine,
                    minMileage = currentFilters.minMileage,
                    maxMileage = currentFilters.maxMileage,
                    minHP = currentFilters.minHP,
                    maxHP = currentFilters.maxHP,
                    minYear = currentFilters.minYear,
                    maxYear = currentFilters.maxYear,
                    ordering = currentFilters.ordering
                )
            } catch (e: TokenExpiredException) {
                // Null check | if null do nothing, if not null run this command (invoke just runs the command)
                onTokenExpired?.invoke()
            } catch (e: Exception) {
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
        category: String?,
        minPrice: Double?,
        maxPrice: Double?,
        minEngine: Int?,
        maxEngine: Int?,
        minMileage: Int?,
        maxMileage: Int?,
        minHP: Int?,
        maxHP: Int?,
        minYear: Int?,
        maxYear: Int?,
        ordering: String?, // Accepts Django sorting keys (e.g., "price", "-price", "year")
        onTokenExpired: (() -> Unit)? = null
    ) {
        // Save the new filter choices into the UI State
        _uiState.update {
            it.copy(
                filters = CarFilterState(
                    category = category,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    minEngine = minEngine,
                    maxEngine = maxEngine,
                    minMileage = minMileage,
                    maxMileage = maxMileage,
                    minHP = minHP,
                    maxHP = maxHP,
                    minYear = minYear,
                    maxYear = maxYear,
                    ordering = ordering
                )
            )
        }
        // Immediately refresh data from server using the newly applied filters
        refreshData(onTokenExpired)
    }

    /**
     * Clears all currently applied filters and sets them back to null
     * Then triggers refreshData() to reload the complete unfiltered
     * car list from the Django database
     */
    fun clearFilters(onTokenExpired: (() -> Unit)? = null) {
        // Reset the filter state back to completely empty
        _uiState.update { it.copy(filters = CarFilterState()) }
        // Fetch the original full list of cars from the server
        refreshData(onTokenExpired)
    }


    // Adds new car entry with a callback if needed for redirection on failure
    // Room triggers observeCars() for auto UI update
    fun addCar(car: CarEntry, onTokenExpired: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Send new car entry to API. If success Room will trigger observeCars() for UI update
                repository.addCarEntry(car)
            } catch (e: TokenExpiredException) {
                onTokenExpired()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to save vehicle.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}