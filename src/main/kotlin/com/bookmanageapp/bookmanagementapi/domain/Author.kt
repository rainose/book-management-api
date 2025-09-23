package com.bookmanageapp.bookmanagementapi.domain

import java.time.LocalDate

// 新規作成用（IDなし）
data class NewAuthor(
    val name: String,
    val birthDate: LocalDate,
    val lockNo: Int = 1,
)

// 永続化済み（IDあり）
data class Author(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
    val lockNo: Int = 1,
)
