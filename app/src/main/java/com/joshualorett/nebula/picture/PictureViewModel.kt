package com.joshualorett.nebula.picture

import androidx.lifecycle.*
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.Resource
import kotlinx.coroutines.launch

/**
 * [ViewModel] for fullscreen picture.
 * Created by Joshua on 1/22/2020.
 */
class PictureViewModel(private val apodRepository: ApodRepository, private val id: Long): ViewModel() {
    private val _picture = MutableLiveData<String>()
    val picture: LiveData<String> = _picture

    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error

    init {
        viewModelScope.launch {
            when(val resource = apodRepository.getCachedApod(id)) {
                is Resource.Success -> {
                    val url = resource.data.hdurl ?: resource.data.url
                    if(url.isEmpty()) {
                        showError("Empty url.")
                    } else {
                        _picture.value = url
                    }
                }
                is Resource.Error -> {
                    showError("Error fetching picture.")
                }
            }
        }
    }

    private fun showError(errorMessage: String) {
        _error.value = errorMessage
    }
}

class PictureViewModelFactory(private val apodRepository: ApodRepository, private val id: Long): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PictureViewModel(apodRepository, id) as T
    }
}