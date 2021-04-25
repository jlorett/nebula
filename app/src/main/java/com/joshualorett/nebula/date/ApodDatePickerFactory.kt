package com.joshualorett.nebula.date

import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Creates a [MaterialDatePicker] to select an APOD submission.
 * Created by Joshua on 5/30/2020.
 */
object ApodDatePickerFactory {
    fun create(selectionDate: LocalDate): MaterialDatePicker<Long> {
        val zoneId = ZoneId.ofOffset("UTC", ZoneOffset.UTC)
        val todayEpochMilli = LocalDate.now().atStartOfDay(zoneId).toInstant().toEpochMilli()
        val selectionDateEpochMilli = selectionDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val earliestDateEpochMilli = LocalDate
            .of(1995, 6, 16)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
        val calConstraints = CalendarConstraints.Builder()
            .setOpenAt(selectionDateEpochMilli)
            .setStart(earliestDateEpochMilli)
            .setEnd(todayEpochMilli)
            .setValidator(ApodDateValidator())
            .build()
        return MaterialDatePicker.Builder.datePicker()
            .setSelection(selectionDateEpochMilli)
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setCalendarConstraints(calConstraints)
            .build()
    }
}
