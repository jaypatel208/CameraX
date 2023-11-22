package com.example.camerax

import java.text.DateFormat
import java.util.Date
import java.util.TimeZone

fun getCurrentDateTimeWithTimeZone(): String {
    val dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT)
    val currentDate = Date()
    val timeZone = TimeZone.getDefault()

    dateFormat.timeZone = timeZone

    return dateFormat.format(currentDate)
}