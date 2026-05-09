package gr.ihu.ict.carshow.auth

// Data class representing the server's response after a successful registration
// Contains the basic profile info of the newly created user
data class RegisterResponse(
    // The unique ID assigned to the user by the database
    val id: Int,
    // Username confirmed by the server
    val username: String,
    // Email address confirmed by the server
    val email: String
)
