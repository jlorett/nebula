package com.joshualorett.nebula.ui.picture

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onStart

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
