package gr.ihu.ict.carshow.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.ihu.ict.carshow.data.model.CarEntry
import gr.ihu.ict.carshow.data.repository.CarRepository
import gr.ihu.ict.carshow.data.rest.TokenExpiredException
import kotlinx.coroutines.launch

class CarDetailViewModel(
    private val repository: CarRepository
) : ViewModel() {


    //Compose state mutableStateOf used for simple state observation
    //Private set ensures can be only modified in this ViewModel
    var car by mutableStateOf<CarEntry?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun getCar(id: Int, onTokenExpired: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                //Checking first on local Room DB if it exists
                val result = repository.getCarById(id)

                if (result == null) {
                    errorMessage = "The car was not found or is unavailable."
                } else {
                    car = result
                }
            } catch (e: TokenExpiredException) {
                onTokenExpired()
            } catch (e: Exception) {
                errorMessage = "Failed to load car details. Please check your connection."
            } finally {
                isLoading = false
            }
        }
    }


    //Resetting the state so on navigation next car doesn't show even briefly old data
    fun clearState() {
        car = null
        errorMessage = null
    }
}