package gr.ihu.ict.carshow.ui.viewmodel

import gr.ihu.ict.carshow.data.model.CarEntry

// Represents entire UI state for the Car List screen
// Includes the fetched data , loading/error states, active search filters
data class CarListUiState(
    // The list of vehicles currently displayed on the screen
    val items: List<CarEntry> = emptyList(),
    // Flag to show a progress indicator while fetching or filtering data
    val isLoading: Boolean = false,
    // Holds network or database error messages to be displayed to the user
    val errorMessage: String? = null,
    // The current state of filters applied by the user, defaults to empty/no filters
    val filters: CarFilterState = CarFilterState()
)

// Holds the active filter and sorting parameters selected by the user in the UI
data class CarFilterState(
    val category: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minEngine: Int? = null,
    val maxEngine: Int? = null,
    val minMileage: Int? = null,
    val maxMileage: Int? = null,
    val minHP: Int? = null,
    val maxHP: Int? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    // Holds the Django ordering key (e.g., "price", "year", "-id")
    val ordering: String? = null
)