package com.joshualorett.nebula.today

import androidx.lifecycle.*
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.OneShotEvent
import com.joshualorett.nebula.shared.Resource
import com.joshualorett.nebula.shared.data
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
    constructor(apodRepository: ApodRepository, bgDispatcher: CoroutineDispatcher, savedStateHandle: SavedStateHandle): this(apodRepository, savedStateHandle) {
        this.bgDispatcher = bgDispatcher
    }
    private val dateKey: String = "date"
    private val _date: MutableStateFlow<LocalDate?> = MutableStateFlow(savedStateHandle.get(dateKey) ?: LocalDate.now())
    private var currentApod: Apod? = null
    private var refresh: Boolean = false
    private var bgDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val _navigateVideoLink: MutableLiveData<OneShotEvent<String?>> = MutableLiveData()
    val navigateVideoLink: LiveData<OneShotEvent<String?>> = _navigateVideoLink
    private val _navigateFullPicture: MutableLiveData<OneShotEvent<Long>> = MutableLiveData()
    val navigateFullPicture: LiveData<OneShotEvent<Long>> = _navigateFullPicture
    private val _showDatePicker: MutableLiveData<OneShotEvent<LocalDate>> = MutableLiveData()
    val showDatePicker: LiveData<OneShotEvent<LocalDate>> = _showDatePicker
    val apod: LiveData<Resource<Apod, String>> = _date
        .asStateFlow()
        .filter { date -> date != null }
        .map { date ->
            val apodDate = date ?: throw NullPointerException("Date can't be null.")
            if (refresh) {
                refresh = false
                apodRepository.getFreshApod(apodDate).flowOn(bgDispatcher).first()
            } else {
                apodRepository.getApod(apodDate).flowOn(bgDispatcher).first()
            }
        }
        .onEach { response ->
            if(response.successful()) {
                currentApod = response.data
            }
        }
        .onStart { emit(Resource.Loading) }
        .catch { throwable -> emit(Resource.Error("Couldn't fetch apod.")) }
        .asLiveData()

    fun videoLinkClicked() {
        currentApod?.let {
            if(it.mediaType == "video") {
                _navigateVideoLink.value = OneShotEvent(it.url)
            }
        }
    }

    fun onPhotoClicked() {
        currentApod?.let {
            _navigateFullPicture.value = OneShotEvent(it.id)
        }
    }

    fun onChooseDate() {
        _showDatePicker.value = OneShotEvent(_date.value ?: LocalDate.now())
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