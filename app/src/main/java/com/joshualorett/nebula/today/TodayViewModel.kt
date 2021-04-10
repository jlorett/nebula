package com.joshualorett.nebula.today

import androidx.lifecycle.*
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.Resource
import com.joshualorett.nebula.shared.data
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
class TodayViewModel @Inject constructor(private val apodRepository: ApodRepository,
                                         private val savedStateHandle: SavedStateHandle): ViewModel() {
    private val dateKey: String = "date"
    private val _date: MutableStateFlow<LocalDate?> = MutableStateFlow(savedStateHandle.get(dateKey) ?: LocalDate.now())
    private var currentApod: Apod? = null
    private var refresh: Boolean = false
    private val _navigateVideoLink: Channel<String?> = Channel()
    val navigateVideoLink: Flow<String?> = _navigateVideoLink.receiveAsFlow()
    private val _navigateFullPicture: Channel<Long> = Channel()
    val navigateFullPicture: Flow<Long> = _navigateFullPicture.receiveAsFlow()
    private val _showDatePicker: Channel<LocalDate> = Channel()
    val showDatePicker: Flow<LocalDate> = _showDatePicker.receiveAsFlow()
    val apod: Flow<Resource<Apod, String>> = _date
        .asStateFlow()
        .filter { date -> date != null }
        .map { date ->
            val apodDate = date ?: throw NullPointerException("Date can't be null.")
            if (refresh) {
                refresh = false
                apodRepository.getFreshApod(apodDate).first()
            } else {
                apodRepository.getApod(apodDate).first()
            }
        }
        .onEach { response ->
            if(response.successful()) {
                currentApod = response.data
            }
        }
        .onStart { emit(Resource.Loading) }
        .catch { throwable -> emit(Resource.Error("Couldn't fetch apod.")) }

    fun videoLinkClicked() {
        currentApod?.let {
            if(it.mediaType == "video") {
                _navigateVideoLink.offer(it.url)
            }
        }
    }

    fun onPhotoClicked() {
        currentApod?.let {
            _navigateFullPicture.offer(it.id)
        }
    }

    fun onChooseDate() {
        _showDatePicker.offer(_date.value ?: LocalDate.now())
    }

    fun updateDate(date: LocalDate) {
        _date.value = null
        _date.value = date
        savedStateHandle.set(dateKey, date)
    }

    fun refresh() {
        refresh = true
        updateDate(_date.value ?: LocalDate.now())
    }
}