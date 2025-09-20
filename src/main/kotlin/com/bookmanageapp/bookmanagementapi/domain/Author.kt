package com.bookmanageapp.bookmanagementapi.domain

import java.time.LocalDate

data class Author(
    val id: Long? = null,
    val name: String,
    val birthDate: LocalDate,
    val lockNo: Int = 1,
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(birthDate.isBefore(LocalDate.now())) { "Birth date must be in the past" }
        require(lockNo > 0) { "Lock number must be positive" }
    }
}
