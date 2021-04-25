package com.joshualorett.nebula.picture

import androidx.lifecycle.*
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

/**
 * [ViewModel] for fullscreen picture.
 * Created by Joshua on 1/22/2020.
 */

@HiltViewModel
class PictureViewModel @Inject constructor(
    apodRepository: ApodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val id = savedStateHandle["id"] ?: -1L
    val picture: Flow<Resource<Apod, String>> = apodRepository.getApod(id)
        .onStart { Resource.Loading }
        .conflate()
}
