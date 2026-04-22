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


    //Private state holder , _uiState is used only on this class locally as uiState
    private val _uiState = MutableStateFlow(CarListUiState())
    //Public read-only state for the UI compose, used asStateFlow() to prevent UI from modifying it directly
    val uiState: StateFlow<CarListUiState> = _uiState.asStateFlow()


    //Runs once when the ViewModel is created (data observation and synchronization)
    init {
        observeCars()
        refreshData()
    }


    //Listens to the Room database for real-time updates
    private fun observeCars() {
        viewModelScope.launch {
            repository.getCarsStream().collect { cars ->
                _uiState.update { it.copy(items = cars) }
            }
        }
    }


    //Synchronize local database with the remote server db
    fun refreshData(onTokenExpired: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                //Repository using refreshCars() fetches data from API and saves to Room
                repository.refreshCars()
            } catch (e: TokenExpiredException) {
                //Null check | if null do nothing, if not null run this command (invoke just runs the command)
                onTokenExpired?.invoke()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to sync with server.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }


    //Adds new car entry with a callback if needed for redirection on failure
    //Room triggers observeCars() for auto UI update
    fun addCar(car: CarEntry, onTokenExpired: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                //Send new car entry to API. If success Room will trigger observeCars() for UI update
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