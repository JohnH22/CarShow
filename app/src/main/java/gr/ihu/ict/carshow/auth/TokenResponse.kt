package gr.ihu.ict.carshow.auth


// Data model for the response received during token refresh process
data class TokenResponse(
    // Newly generated short-lived Access Token
    val access: String,
    // Refresh token may be null if server not configured for "Token Rotation"
    // If null the app continues using the existing Refresh Token
    val refresh: String? = null
)
