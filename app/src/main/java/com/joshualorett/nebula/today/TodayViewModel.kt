package com.joshualorett.nebula.today

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.OneShotEvent
import com.joshualorett.nebula.shared.Resource
import com.joshualorett.nebula.shared.data
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.LocalDate

/**
 * [ViewModel] show today's Astronomy Picture of the Day.
 * Created by Joshua on 1/11/2020.
 */
class TodayViewModel @ViewModelInject constructor(private val apodRepository: ApodRepository): ViewModel() {
    constructor(apodRepository: ApodRepository, bgDispatcher: CoroutineDispatcher): this(apodRepository) {
        this.bgDispatcher = bgDispatcher
    }
    private var currentApod: Apod? = null
    private val today = LocalDate.now()
    private val _date = MutableStateFlow(today)
    private var refresh = false
    private var bgDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val _navigateVideoLink: MutableLiveData<OneShotEvent<String?>> = MutableLiveData()
    val navigateVideoLink: LiveData<OneShotEvent<String?>> = _navigateVideoLink
    private val _navigateFullPicture: MutableLiveData<OneShotEvent<Long>> = MutableLiveData()
    val navigateFullPicture: LiveData<OneShotEvent<Long>> = _navigateFullPicture
    private val _showDatePicker: MutableLiveData<OneShotEvent<LocalDate>> = MutableLiveData()
    val showDatePicker: LiveData<OneShotEvent<LocalDate>> = _showDatePicker
    val apod = _date
        .filter { date -> date != null }
        .flatMapLatest {  date ->
            if(refresh) {
                refresh = false
                apodRepository.getFreshApod(date)
            } else {
                apodRepository.getApod(date)
            }
        }
        .onEach { res ->
            currentApod = res.data
        }
        .onStart { emit(Resource.Loading) }
        .catch { throwable -> emit(Resource.Error("Couldn't fetch apod.")) }
        .flowOn(bgDispatcher)
        .conflate()
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
        _date.value = date
    }

    fun refresh() {
        refresh = true
        _date.value?.let {
            updateDate(it)
        }
    }
}