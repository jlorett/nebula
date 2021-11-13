package com.joshualorett.nebula.ui.picture

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

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
    val picture: StateFlow<Resource<Apod, String>> = flow {
        emit(apodRepository.getCachedApod(id))
    }.stateIn(
        initialValue = Resource.Loading,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )
}
