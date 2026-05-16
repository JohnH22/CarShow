package gr.ihu.ict.carshow.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import gr.ihu.ict.carshow.data.local.CarEntity
import gr.ihu.ict.carshow.data.local.CarEntryDao
import gr.ihu.ict.carshow.data.local.ReviewEntity
import gr.ihu.ict.carshow.data.model.CarCategory
import gr.ihu.ict.carshow.data.model.CarEntry
import gr.ihu.ict.carshow.data.model.ReviewRequest
import gr.ihu.ict.carshow.data.model.SellerType
import gr.ihu.ict.carshow.data.model.VehicleReview
import gr.ihu.ict.carshow.data.rest.CarEntryApiService
import gr.ihu.ict.carshow.data.rest.CarEntryDto
import gr.ihu.ict.carshow.data.rest.TokenExpiredException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.ByteArrayOutputStream

// Manages data coordination between the remote network API (Retrofit) and local database (Room)
class RestCarRepository(
    private val apiService: CarEntryApiService,
    private val dao: CarEntryDao

): CarRepository {


    // Fetches all cars from local Room DB as a Flow and maps database entities to UI-ready CarEntry models
    override fun getCarsStream(): Flow<List<CarEntry>> {

        return dao.getAllCars().map { entities ->
            entities.map { entityToCarEntry(it) }
        }
    }



    // Streams local car entries filtered by category, converting them to UI-ready CarEntry models
    override fun getCarsByCategoryStream(category: CarCategory): Flow<List<CarEntry>> {

        return dao.getCarsByCategory(category.name).map { entities ->
            entities.map { entityToCarEntry(it) }
        }
    }


    // Synchronizes local database (Room) with the server by downloading all vehicles and saving them to Room
    override suspend fun refreshCars() {
        try {
            val remote = apiService.getAllCarEntries()

            dao.insertAllCars(remote.map { dtoToEntity(it) })
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw TokenExpiredException()
            }
            throw e
        }
    }



    // Retrieves a vehicle ID and checks Room database first for an instant response from cache
    // If missing falls back to the API network call, caches the result and returns it
    override suspend fun getCarById(id: Int): CarEntry? {
        val cached = dao.getCarById(id)

        // Cache-first. Return immediately if data exists locally
        if (cached != null)
            return entityToCarEntry(cached)

        return try {
            val remote = apiService.getById(id)
            val entity = dtoToEntity(remote)
            dao.insertCar(entity) // Update local storage with the freshly fetched item
            entityToCarEntry(entity)
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 401) {
                throw TokenExpiredException()
            }
            null
        }
    }


    // Converts the vehicle's bitmap image into a compressed byte array
    // Packages the byte array to DTO and sends it to the server before updating Room
    override suspend fun addCarEntry(carEntry: CarEntry) {
        val stream = ByteArrayOutputStream()
        carEntry.mainImage.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val imageBytes = stream.toByteArray()


        val dto = carEntryToDto(carEntry, imageBytes)

        try {
            val saved = apiService.addCarEntry(dto)

            dao.insertCar(dtoToEntity(saved)) // Saves newly added car entry into local database

        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw TokenExpiredException()
            }
            throw e
        }
    }


    // Deletes a vehicle entry from both the remote server and the local Room database
    override suspend fun deleteCar(carEntry: CarEntry) {
        try {
            apiService.deleteCarEntry(carEntry.id)
            dao.deleteCarById(carEntry.id)
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw TokenExpiredException()
            }
            throw e
        }
    }

    // Observes the local database reviews table
    // Provides them as a Flow (live stream) of reviews for the UI
    override fun getReviewsStream(vehicleId: Int): Flow<List<VehicleReview>> {
        return dao.getReviewsForVehicle(vehicleId).map { entities ->
            entities.map { entity ->
                VehicleReview(
                    username = entity.username,
                    rating = entity.rating,
                    comment = entity.comment,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    // Retrieves the latest reviews from the remote API
    // Overwrites or inserts them to Room and returns them
    override suspend fun getVehicleReviews(id: Int): List<VehicleReview> {
        var remoteReviews: List<VehicleReview> = emptyList()

        try {
            remoteReviews = apiService.getVehicleReviews(id)

            // Saves the reviews got from the remote API into local Room database
            dao.insertReviews(remoteReviews.map { dto ->
                ReviewEntity(
                    vehicleId = id,
                    username = dto.username,
                    rating = dto.rating,
                    comment = dto.comment,
                    createdAt = dto.createdAt
                )
            })
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw TokenExpiredException()
            }
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return remoteReviews
    }


    // Posts a new review via network API , then saves a copy to the local Room database
    // Ensures immediate feedback thanks to reactive flow
    override suspend fun postReview(id: Int, request: ReviewRequest): VehicleReview {
        try {
            val newReview = apiService.postReview(id, request)

            // Cache the newly created review item locally
            dao.insertSingleReview(
                ReviewEntity(
                    vehicleId = id,
                    username = newReview.username,
                    rating = newReview.rating,
                    comment = newReview.comment,
                    createdAt = newReview.createdAt
                )
            )
            return newReview
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw TokenExpiredException()
            }
            throw e
        }
    }


    // Mapper: Converts a network DTO object into a local database entity
    // Provides default safe-fallbacks for null values received from backend
    private fun dtoToEntity(dto: CarEntryDto): CarEntity {

        return CarEntity(
            id = dto.id,
            brand = dto.brand ?: "",
            model = dto.model ?: "",
            category = dto.category ?: CarCategory.UNKNOWN.name,
            year = dto.year ?: 0,
            price = dto.price ?: 0.0,
            priceNegotiable = dto.priceNegotiable ?: false,
            mainImageData = dto.mainImageData ?: ByteArray(0),
            description = dto.description ?: "",
            engine = dto.engine ?: "",
            horsepower = dto.horsepower ?: 0,
            drivetrain = dto.drivetrain ?: "",
            transmission = dto.transmission ?: "",
            torque = dto.torque ?: 0,
            consumption = dto.consumption?.toDoubleOrNull() ?: 0.0,
            mileage = dto.mileage ?: 0,
            interiorColor = dto.interiorColor ?: "",
            exteriorColor = dto.exteriorColor ?: "",
            wheelSize = dto.wheelSize ?: 0,
            doors = dto.doors ?: 0,
            passengers = dto.passengers ?: 0,
            isRightHandDrive = dto.isRightHandDrive ?: false,
            location = dto.location ?: "",
            sellerType = dto.sellerType ?: SellerType.UNKNOWN.name,
            rating = dto.rating?.toDouble() ?: 0.0,
            videoUrl = dto.videoUrl ?: ""
        )
    }



    // Mapper: Converts a Local Database Entity into a UI-ready CarEntry model
    // Decodes binary byte arrays back into renderable Android Bitmaps
    private fun entityToCarEntry(entity: CarEntity): CarEntry {
        val bitmap = BitmapFactory.decodeByteArray(
            entity.mainImageData,
            0,
            entity.mainImageData.size
        ) ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Safe fallback blank pixel bitmap


        return CarEntry(
            id = entity.id,
            brand = entity.brand,
            model = entity.model,
            category = CarCategory.valueOf(entity.category),
            year = entity.year,
            price = entity.price,
            priceNegotiable = entity.priceNegotiable,
            mainImage = bitmap,
            description = entity.description,
            engine = entity.engine,
            horsepower = entity.horsepower,
            drivetrain = entity.drivetrain,
            transmission = entity.transmission,
            torque = entity.torque,
            consumption = entity.consumption.toString(),
            mileage = entity.mileage,
            interiorColor = entity.interiorColor,
            exteriorColor = entity.exteriorColor,
            wheelSize = entity.wheelSize,
            doors = entity.doors,
            passengers = entity.passengers,
            isRightHandDrive = entity.isRightHandDrive,
            location = entity.location,
            sellerType = SellerType.valueOf(entity.sellerType),
            rating = entity.rating.toFloat(),
            videoUrl = entity.videoUrl
        )
    }



    // Mapper: Converts a UI-ready model into a Network Payload DTO
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


//    Its useful only if Flow is not used and refreshCars but instead using a simple List.
//    private fun dtoToCarEntry(dto: CarEntryDto): CarEntry {
//        return entityToCarEntry(dtoToEntity(dto))
//    }
}