package gr.ihu.ict.carshow.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.graphics.Matrix
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import android.util.Base64



// Converts a selected image URI into a Base64 encoded string ready for JSON transmission.
// Handles bitmap decoding based on the Android API version and applies necessary rotation.
fun processImageToBase64(context: Context, uri: Uri): String? {
    return try {
        // Decode the URI into a Bitmap based on the device's SDK version
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        // Corrects orientation based on EXIF metadata before processing
        val rotatedBitmap = rotateBitmapIfNeeded(uri, context, bitmap)

        // Compress the bitmap to JPEG format (70% quality) and convert to byte array
        val outputStream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val bytes = outputStream.toByteArray()

        // Return the final Base64 string representation
        Base64.encodeToString(bytes, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}



//Check the EXIF metadata of an image URI and rotate the bitmap
// to the correct orientation if needed
fun rotateBitmapIfNeeded(uri: Uri, context: Context, bitmap: Bitmap): Bitmap {
    //Open an input stream for the URI to read the EXIF data (orientation tags)
    val exif = context.contentResolver
        .openInputStream(uri)?.use { ExifInterface(it) }
        ?: return bitmap


    //Determine the degree of rotation based on the orientation attribute
    val rotation = when (
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }


    //If no rotation is required return the original bitmap (save memory)
    if (rotation == 0f) return bitmap



    //Apply the rotation matrix to the bitmap
    val matrix = Matrix().apply { postRotate(rotation) }
    return Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
    )
}