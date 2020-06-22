package com.joshualorett.nebula.picture

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.Resource
import kotlinx.coroutines.launch

/**
 * [ViewModel] for fullscreen picture.
 * Created by Joshua on 1/22/2020.
 */

class PictureViewModel @ViewModelInject constructor(private val apodRepository: ApodRepository): ViewModel() {
    private val _picture = MutableLiveData<Resource<Apod, String>>()
    val picture: LiveData<Resource<Apod, String>> = _picture

    fun load(id: Long) {
        viewModelScope.launch {
            _picture.value = Resource.Loading
            _picture.value = apodRepository.getCachedApod(id)
        }
    }
}