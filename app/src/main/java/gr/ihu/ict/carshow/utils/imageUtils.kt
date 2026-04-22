package gr.ihu.ict.carshow.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface


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