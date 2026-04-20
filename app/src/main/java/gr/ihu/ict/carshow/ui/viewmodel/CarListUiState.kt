package gr.ihu.ict.carshow.ui.viewmodel

import gr.ihu.ict.carshow.data.model.CarEntry

data class CarListUiState(
    val items: List<CarEntry> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
