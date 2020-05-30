package com.joshualorett.nebula.date

import android.os.Parcel
import android.os.Parcelable
import com.google.android.material.datepicker.CalendarConstraints
import java.time.*

/**
 * Validates if date falls within a valid APOD submission.
 * Created by Joshua on 5/29/2020.
 */
class ApodDateValidator() : CalendarConstraints.DateValidator {
    // The first APOD was 1995-06-16.
    private val earliestDate: LocalDate = LocalDate.of(1995, 6, 16)

    constructor(parcel: Parcel) : this() {}

    override fun isValid(date: Long): Boolean {
        val today = LocalDate.now()
        val localDate = Instant.ofEpochMilli(date).atOffset(ZoneOffset.UTC).toLocalDate()
        return (earliestDate.isBefore(localDate) or earliestDate.isEqual(localDate)) and
                (localDate.isBefore(today) or localDate.isEqual(today))
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ApodDateValidator> {
        override fun createFromParcel(parcel: Parcel): ApodDateValidator {
            return ApodDateValidator(parcel)
        }

        override fun newArray(size: Int): Array<ApodDateValidator?> {
            return arrayOfNulls(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {}
}