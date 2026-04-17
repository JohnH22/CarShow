package gr.ihu.ict.carshow.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CarEntryDao {

    @Query("SELECT * FROM car_entries ORDER BY brand ASC")
    fun getAllCars(): Flow<List<CarEntity>>

    @Query("SELECT * FROM car_entries WHERE id = :id")
    suspend fun getCarById(id: Int): CarEntity?

    @Query("SELECT * FROM car_entries WHERE category = :category")
    fun getCarsByCategory(category: String): Flow<List<CarEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCar(car: CarEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCars(cars: List<CarEntity>)

    @Query("DELETE FROM car_entries WHERE id = :id")
    suspend fun deleteCarById(id: Int)

    @Delete
    suspend fun deleteCar(car: CarEntity)

    @Query("DELETE FROM car_entries")
    suspend fun deleteAll()
}