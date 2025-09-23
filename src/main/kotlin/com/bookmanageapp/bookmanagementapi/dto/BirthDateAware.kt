package com.bookmanageapp.bookmanagementapi.dto

import java.time.LocalDate

interface BirthDateAware {
    val birthDate: LocalDate
    val clientTimeZone: String
}
