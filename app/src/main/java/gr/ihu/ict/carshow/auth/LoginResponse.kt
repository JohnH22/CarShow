package gr.ihu.ict.carshow.auth


// Data model representing the server response after a successful login
// Follows the standard SimpleJWT/OAuth2 format
data class LoginResponse(
    // Short-lived token used for authenticating API requests
    val access: String,
    // Long-lived token used to obtain a new Access Token when expired
    val refresh: String
)
