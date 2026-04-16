package gr.ihu.ict.carshow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [CarEntity::class],
    version = 1
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun carEntryDao(): CarEntryDao
}