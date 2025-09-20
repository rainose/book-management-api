package com.bookmanageapp.bookmanagementapi.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class Author(
    val id: Long? = null,
    val name: String,
    val birthDate: LocalDate,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(birthDate.isBefore(LocalDate.now())) { "Birth date must be in the past" }
    }
}
