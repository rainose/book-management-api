package com.bookmanageapp.bookmanagementapi.dto

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
)
