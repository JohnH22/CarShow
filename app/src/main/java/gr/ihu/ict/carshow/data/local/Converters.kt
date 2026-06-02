package gr.ihu.ict.carshow.data.local

import androidx.room.TypeConverter

class Converters {
    // Converts a list of image URLs into a single comma-separated String for Room database storage
    @TypeConverter
    fun fromListToString(list: List<String>): String {
        return list.joinToString(separator = ",")
    }

    // Reconstructs the comma-separated String back into a clean List of URLs for app usage
    @TypeConverter
    fun fromStringToList(value: String): List<String> {
        // Return an empty list immediately if the stored value is blank
        if (value.isEmpty()) return emptyList()

        // Splits the single string back into individual URLs using the comma as a guide
        // Example: "url1,url2" becomes a clean list ["url1", "url2"]
        return value.split(",")
    }
}