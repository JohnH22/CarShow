package gr.ihu.ict.carshow.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import gr.ihu.ict.carshow.data.model.CarCategory
import gr.ihu.ict.carshow.data.model.CarEntry
import gr.ihu.ict.carshow.data.model.SellerType
import gr.ihu.ict.carshow.data.rest.CarEntryApiService
import gr.ihu.ict.carshow.data.rest.CarEntryDto
import gr.ihu.ict.carshow.data.rest.TokenExpiredException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.ByteArrayOutputStream

class RestCarRepository(
    private val apiService: CarEntryApiService
): CarRepository {


    override fun getCarsStream(): Flow<List<CarEntry>> = flow {
        try {
            val remoteDtos = apiService.getAllCarEntries()
            emit(remoteDtos.map { dtoToCarEntry(it) })
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw TokenExpiredException()
            }
            throw e
        }
    }



    override fun getCarsByCategoryStream(category: CarCategory): Flow<List<CarEntry>> = flow {
        try {
            val remoteDtos = apiService.getByCategory(category.name)
            emit(remoteDtos.map { dtoToCarEntry(it) })
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw TokenExpiredException()
            }
            throw e
        }
    }



    override suspend fun getCarById(id: Int): CarEntry? {
        return try {
            val dto = apiService.getById(id)
            dtoToCarEntry(dto)
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw TokenExpiredException()
            }
            null
        }
    }


    override suspend fun addCarEntry(carEntry: CarEntry) {
        val stream = ByteArrayOutputStream()
        carEntry.mainImage.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val imageBytes = stream.toByteArray()


        val dto = carEntryToDto(carEntry, imageBytes)

        try {
            apiService.addCarEntry(dto)

        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw TokenExpiredException()
            }
            throw e
        }
    }


    override suspend fun deleteCar(carEntry: CarEntry) {
        try {
            apiService.deleteCarEntry(carEntry.id)
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw TokenExpiredException()
            }
            throw e
        }
    }

    private fun dtoToCarEntry(dto: CarEntryDto): CarEntry {
        val bitmap = BitmapFactory.decodeByteArray(dto.mainImageData, 0, dto.mainImageData.size)
            ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

        return CarEntry(
            id = dto.id,
            brand = dto.brand,
            model = dto.model,
            category = CarCategory.valueOf(dto.category),
            year = dto.year,
            price = dto.price,
            priceNegotiable = dto.priceNegotiable,
            mainImage = bitmap,
            description = dto.description,
            engine = dto.engine,
            horsepower = dto.horsepower,
            drivetrain = dto.drivetrain,
            transmission = dto.transmission,
            torque = dto.torque,
            consumption = dto.consumption,
            mileage = dto.mileage,
            interiorColor = dto.interiorColor,
            exteriorColor = dto.exteriorColor,
            wheelSize = dto.wheelSize,
            doors = dto.doors,
            passengers = dto.passengers,
            isRightHandDrive = dto.isRightHandDrive,
            location = dto.location,
            sellerType = SellerType.valueOf(dto.sellerType),
            rating = dto.rating,
            videoUrl = dto.videoUrl
        )
    }



    private fun carEntryToDto(car: CarEntry, imageBytes: ByteArray): CarEntryDto {
        return CarEntryDto(
            id = car.id,
            brand = car.brand,
            model = car.model,
            category = car.category.name,
            year = car.year,
            price = car.price,
            priceNegotiable = car.priceNegotiable,
            mainImageData = imageBytes,
            description = car.description,
            engine = car.engine,
            horsepower = car.horsepower,
            drivetrain = car.drivetrain,
            transmission = car.transmission,
            torque = car.torque,
            consumption = car.consumption,
            mileage = car.mileage,
            interiorColor = car.interiorColor,
            exteriorColor = car.exteriorColor,
            wheelSize = car.wheelSize,
            doors = car.doors,
            passengers = car.passengers,
            isRightHandDrive = car.isRightHandDrive,
            location = car.location,
            sellerType = car.sellerType.name,
            rating = car.rating,
            videoUrl = car.videoUrl
        )
    }
}