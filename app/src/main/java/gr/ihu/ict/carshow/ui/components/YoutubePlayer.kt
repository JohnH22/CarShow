package gr.ihu.ict.carshow.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView




// Composable function that displays a preview card for a YouTube video
// Clicking the card launches an external intent to watch the video on the YouTube app or browser
@Composable
fun YoutubePlayer(
    videoUrl: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Extract the unique 11-character video ID to fetch the corresponding preview thumbnail
    val videoId = remember(videoUrl) {
        extractYoutubeVideoId(videoUrl)
    }

    if (videoId != null && !videoUrl.isNullOrBlank()) {
        // Official Google URL endpoint to retrieve the high-quality (hqdefault) video image thumbnail
        val thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    launchYoutubeIntent(context, videoUrl)
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Image Preview (Thumbnail) asynchronously loaded using Coil
                Image(
                    painter = rememberAsyncImagePainter(model = thumbnailUrl),
                    contentDescription = "Video Thumbnail",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark overlay scrim to increase contrast for the Play icon button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )

                // Iconic Play Button aligned perfectly in the center of the card layout
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = "Play Video",
                    tint = Color(0xFFFF0000), // YouTube Red
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center)
                )

                // Small informational text hint positioned at the bottom of the video box
                Text(
                    text = "Watch on YouTube",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    } else {
        // Fallback layout block displayed when the provided video URL string is malformed or invalid
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFF2C1414), RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Invalid Video Link provided.",
                color = Color(0xFFEF5350),
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
    }
}


// Fires an implicit ACTION_VIEW Intent targeting the YouTube player platform or mobile web browser
fun launchYoutubeIntent(context: Context, videoUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)).apply {
            // This flag ensures that the external target launches as a completely separate runtime task
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open video link", Toast.LENGTH_SHORT).show()
    }
}

 /**
  * Helper function to safely extract the 11-character video ID from any YouTube URL
  */
fun extractYoutubeVideoId(url: String?): String? {
    val cleanedUrl = url?.trim()
    if (cleanedUrl.isNullOrBlank()) return null
    return try {
        when {
            cleanedUrl.contains("watch?v=") -> cleanedUrl.substringAfter("watch?v=").substringBefore("&")
            cleanedUrl.contains("youtu.be/") -> cleanedUrl.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
            cleanedUrl.contains("embed/") -> cleanedUrl.substringAfter("embed/").substringBefore("?").substringBefore("&")
            cleanedUrl.length == 11 -> cleanedUrl
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}
