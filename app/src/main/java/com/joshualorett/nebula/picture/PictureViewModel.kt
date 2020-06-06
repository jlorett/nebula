package com.joshualorett.nebula.picture

import androidx.lifecycle.*
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.Resource
import kotlinx.coroutines.launch

/**
 * [ViewModel] for fullscreen picture.
 * Created by Joshua on 1/22/2020.
 */
class PictureViewModel(private val apodRepository: ApodRepository, private val id: Long): ViewModel() {
    private val _picture = MutableLiveData<Resource<Apod, String>>()
    val picture: LiveData<Resource<Apod, String>> = _picture

    init {
        viewModelScope.launch {
            _picture.value = Resource.Loading
            _picture.value = apodRepository.getCachedApod(id)
        }
    }
}

class PictureViewModelFactory(private val apodRepository: ApodRepository, private val id: Long): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PictureViewModel(apodRepository, id) as T
    }
}