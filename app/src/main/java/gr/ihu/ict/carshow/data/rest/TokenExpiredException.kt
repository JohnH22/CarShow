package gr.ihu.ict.carshow.data.rest
import java.io.IOException


// Custom Exception used to signal that both Access and Refresh Tokens have expired
// It inherits from IOException because Retrofit and OkHttp treat network-related
// authentication failures as I/O errors during request-response cycle
class TokenExpiredException: IOException("Token Expired. Please log in again.") {
    // This Exception acts as trigger for the UI to clear the session
    // Redirect the user back to Login Screen
}