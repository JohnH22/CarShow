package gr.ihu.ict.carshow.ui.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YoutubePlayer(
    youtubeVideoId: String,
    modifier: Modifier = Modifier
) {
    // Get the current LifecycleOwner so the video pauses when user minimizes the app
    val lifecycleOwner = LocalLifecycleOwner.current

    // Using AndroidView to host the XML/View-based YouTubePlayerView
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f), // standard widescreen format for videos
        factory = { context ->
            YouTubePlayerView(context).apply {
                // Link the player to the lifecycle of the composable screen
                lifecycleOwner.lifecycle.addObserver(this)

                // Add listener to load the video once the player is ready
                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        // cueVideo loads the thumbnail without autoplaying
                        // If you want to autoplay, use loadVideo(youtubeVideoId, 0f)
                        youTubePlayer.cueVideo(youtubeVideoId, 0f)
                    }
                })
            }
        }
    )
}

/**
 * Helper function to safely extract the 11-character video ID from any YouTube URL
 * Returns null if the URL is invalid or empty
*/
fun extractYoutubeVideoId(url: String?): String? {
    // Return null immediately if the input URL is null, empty or just spaces
    if (url.isNullOrBlank()) return null
    return when {
        // Handle standard desktop URLs: https://www.youtube.com/watch?v=VIDEO_ID
        url.contains("v=") -> url.substringAfter("v=").substringBefore("&")

        // Handle shortened mobile URLs: https://youtu.be/VIDEO_ID
        url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?")

        // Return null if the string doesn't match known YouTube patterns
        else -> null
    }
}