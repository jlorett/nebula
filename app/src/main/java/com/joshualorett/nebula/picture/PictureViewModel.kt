package com.joshualorett.nebula.picture

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * [ViewModel] for fullscreen picture.
 * Created by Joshua on 1/22/2020.
 */

class PictureViewModel @ViewModelInject constructor(private val apodRepository: ApodRepository): ViewModel() {
    private val id = MutableStateFlow(-1L)
    var bgDispatcher: CoroutineDispatcher = Dispatchers.IO

    val picture: LiveData<Resource<Apod, String>> = id.filter { id -> id > -1L }
        .flatMapLatest { id ->
            apodRepository.getCachedApod(id)
        }
        .onStart { emit(Resource.Loading) }
        .flowOn(bgDispatcher)
        .conflate()
        .asLiveData()

    fun load(id: Long) {
        this.id.value = id
    }
}