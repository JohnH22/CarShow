package gr.ihu.ict.carshow.data.local

import android.content.Context
import androidx.room.Room


object DatabaseProvider {

    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "car_show_database"
                // Clears all data and rebuilds the database when the Entity structure is modified
            ).fallbackToDestructiveMigration()
                .build()

            INSTANCE = instance
            instance
        }
    }
}