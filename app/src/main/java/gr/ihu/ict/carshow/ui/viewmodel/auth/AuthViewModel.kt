package gr.ihu.ict.carshow.ui.viewmodel.auth

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import gr.ihu.ict.carshow.auth.LoginRequest
import gr.ihu.ict.carshow.auth.RegisterRequest
import gr.ihu.ict.carshow.auth.TokenStore
import gr.ihu.ict.carshow.data.rest.CarEntryApiService
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


// AuthViewModel handles authentication logic, login , logout and session state management (catch exceptions)
class AuthViewModel(
    application: Application,
    private val api: CarEntryApiService
) : AndroidViewModel(application) {


    // Accessing application context
    private val context: Context get() = getApplication<Application>()

    // Boolean to trigger navigation once login is successful
    var loginSuccess by mutableStateOf(false)
        private set

    // Error messages to be displayed on the UI
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Used to indicate network request currently in progress (spinner)
    var isLoading by mutableStateOf(false)
        private set


    // Attempt to authenticate the user
    // On Success save the tokens and update UI state
    fun login(username: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null // Reset errors before starting
            try {
                // Performing network request
                val response = api.login(LoginRequest(username, password))
                // Save the tokens using TokenStore
                TokenStore.saveTokens(context, response.access, response.refresh)
                // Notify UI that navigation can proceed
                loginSuccess = true
            } catch (e: HttpException) {
                // Server responded with an error code (401, 403, 500, ....)
                errorMessage = when (e.code()) {
                    401 -> "Wrong Username or Password. Please check your log-in credentials."
                    403 -> "Account is disabled or lacks permission."
                    500 -> "Server error. Please try again later."
                    else -> "An unexpected error occurred. (Error: ${e.code()})"
                }
            } catch (e: IOException) {
                // Network or connection issues (no internet, timeout, ....)
                errorMessage = "No internet connection. Please check your internet connection."
            } catch (e: Exception) {
                // Any other errors
                errorMessage = "Something went wrong. Please try again."
            }
            finally {
                // Making sure loading stops regardless success or failure
                isLoading = false
            }
        }
    }


    // Handles the user registration process
    // After successful signup , automatically log the user in
    fun register(username: String, email: String, password: String) {
        // Run local validation first
        // If validateInput returns FALSE the !(NOT) turns it into TRUE
        // The "if(true)" executes the "return" stopping the register function
        // If validateInput returns TRUE , the "!" turns it into FALSE
        // The "if" is ignored and proceeds to next block of code
        if (!validateInput(username, email, password)) return

        // If valid proceed with the network call
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Call API to create the user account
                val response = api.register(RegisterRequest(username, email, password))

                // Since registration was successful proceed to login
                // So user doesn't have to type their credentials twice.
                login(username, password)

            } catch (e: HttpException) {
                // HTTP error handling(400 Bad Request if the user already exists)
                errorMessage = when (e.code()) {
                    400 -> "Username or Email already taken."
                    else -> "Registration failed (Error: ${e.code()})."
                }
            } catch (e: IOException) {
                // Connectivity issues
                errorMessage = "Connection issue. Please check your internet connection and try again."
            } catch (e: Exception) {
                // Any other type of errors
                errorMessage = "An unexpected error occurred."
            } finally {
                isLoading = false
            }
        }
    }


    // Local validation to catch errors before going to the API
    // Updates errorMessage if validation fails
    private fun validateInput(username: String, email: String, password: String): Boolean {
        return when {
            // When one of them is empty give the error message
            username.isBlank() || email.isBlank() || password.isBlank() -> {
                errorMessage = "Please fill in all fields."
                false // This "false" is returned to signal validation failure
            }
            // When the email address the user typed doesn't match an email address format give error message
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                errorMessage = "Invalid email format."
                false // Signals that email check failed
            }
            // When the password is less than 8 characters give error message
            password.length < 8 -> {
                errorMessage = "Password must be at least 8 characters long."
                false // Signals that password check failed
            }
            else -> true // If none of the above return "true" (Success)
        }
    }

    // Performs logout by wiping stored credentials and notifying the navigation controller
    fun logout(onDone: () -> Unit) {
        TokenStore.clear(context)
        onDone()
    }

    // Reset success flag so UI doesn't auto-navigate when returns to the Login Screen
    fun resetLoginSuccess() {
        loginSuccess = false
    }
}