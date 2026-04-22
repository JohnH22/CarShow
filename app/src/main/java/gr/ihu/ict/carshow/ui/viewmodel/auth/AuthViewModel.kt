package gr.ihu.ict.carshow.ui.viewmodel.auth

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import gr.ihu.ict.carshow.auth.LoginRequest
import gr.ihu.ict.carshow.auth.TokenStore
import gr.ihu.ict.carshow.data.rest.CarEntryApiService
import kotlinx.coroutines.launch

class AuthViewModel(
    application: Application,
    private val api: CarEntryApiService
) : AndroidViewModel(application) {


    private val context: Context get() = getApplication<Application>()

    var loginSuccess by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set


    fun login(username: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = api.login(LoginRequest(username, password))
                TokenStore.save(context, response.token)
                loginSuccess = true
            } catch (e: Exception) {
                errorMessage = "Login failed. Check your credentials."
            } finally {
                isLoading = false
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        TokenStore.clear(context)
        onDone()
    }

    fun resetLoginSuccess() {
        loginSuccess = false
    }
}