package com.bookmanageapp.bookmanagementapi.dto

import kotlin.math.ceil

data class PagedResponse<T>(
    val content: List<T>,
    val pagination: PaginationInfo,
)

data class PaginationInfo(
    val currentPage: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
) {
    companion object {
        fun fromPageNumber(page: Int, size: Int, totalElements: Long): PaginationInfo {
            val totalPages = if (totalElements > 0) ceil(totalElements.toDouble() / size).toInt() else 0
            return PaginationInfo(
                currentPage = page,
                pageSize = size,
                totalElements = totalElements,
                totalPages = totalPages,
                hasNext = page < totalPages,
                hasPrevious = page > 1,
            )
        }

    }
}
