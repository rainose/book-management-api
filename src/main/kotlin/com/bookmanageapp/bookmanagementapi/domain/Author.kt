package com.bookmanageapp.bookmanagementapi.domain

import java.time.LocalDate

data class Author(
    val id: Long? = null,
    val name: String,
    val birthDate: LocalDate,
    val lockNo: Int = 1,
)
