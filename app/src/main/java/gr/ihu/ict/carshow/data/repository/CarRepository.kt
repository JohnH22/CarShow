package gr.ihu.ict.carshow.data.repository

import gr.ihu.ict.carshow.data.model.CarEntry
import gr.ihu.ict.carshow.data.model.CarCategory
import kotlinx.coroutines.flow.Flow

interface CarRepository {
    fun getCarsStream(): Flow<List<CarEntry>>
    fun getCarsByCategoryStream(category: CarCategory): Flow<List<CarEntry>>
    suspend fun getCarById(id: Int): CarEntry?
    suspend fun addCarEntry(carEntry: CarEntry)
    suspend fun deleteCar(carEntry: CarEntry)
}