package com.joshualorett.nebula.ui.today

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

/**
 * [ViewModel] show today's Astronomy Picture of the Day.
 * Created by Joshua on 1/11/2020.
 */
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val apodRepository: ApodRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val dateKey: String = "date"
    private var refresh: Boolean = false
    private val _navigateVideoLink: Channel<String?> = Channel()
    val navigateVideoLink: Flow<String?> = _navigateVideoLink.receiveAsFlow()
    private val _navigateFullPicture: Channel<Long> = Channel()
    val navigateFullPicture: Flow<Long> = _navigateFullPicture.receiveAsFlow()
    private val _showDatePicker: Channel<LocalDate> = Channel()
    val showDatePicker: Flow<LocalDate> = _showDatePicker.receiveAsFlow()
    private val date: MutableStateFlow<LocalDate?> = MutableStateFlow(
        savedStateHandle.get(dateKey) ?: LocalDate.now()
    )
    val apod: StateFlow<Resource<Apod, String>> = date
        .asStateFlow()
        .filterNotNull()
        .map { date ->
            if (refresh) {
                refresh = false
                apodRepository.getFreshApod(date)
            } else {
                apodRepository.getApod(date)
            }
        }
        .catch { emit(Resource.Error("Couldn't fetch apod.")) }
        .stateIn(
            initialValue = Resource.Loading,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    fun videoLinkClicked() {
        val resource = apod.value
        if (resource.successful()) {
            val apod = (resource as Resource.Success).data
            if (apod.mediaType == "video") {
                _navigateVideoLink.trySend(apod.url)
            }
        }
    }

    fun onPhotoClicked() {
        val resource = apod.value
        if (resource.successful()) {
            val apod = (resource as Resource.Success).data
            _navigateFullPicture.trySend(apod.id)
        }
    }

    fun onChooseDate() {
        _showDatePicker.trySend(date.value ?: LocalDate.now())
    }

    fun updateDate(date: LocalDate) {
        this.date.value = null
        this.date.value = date
        savedStateHandle.set(dateKey, date)
    }

    fun refresh() {
        refresh = true
        updateDate(date.value ?: LocalDate.now())
    }
}
