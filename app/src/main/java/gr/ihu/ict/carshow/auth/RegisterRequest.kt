package gr.ihu.ict.carshow.auth

// Data class representing the registration request
// Sent to server when a new user attempts to sign up
data class RegisterRequest(
    // Username chosen by user
    val username: String,
    // User's email address
    val email: String,
    // User's plain text password
    val password: String
)
