package com.bookmanageapp.bookmanagementapi.controller

import com.bookmanageapp.bookmanagementapi.dto.BookResponse
import com.bookmanageapp.bookmanagementapi.dto.CreateBookRequest
import com.bookmanageapp.bookmanagementapi.dto.PagedResponse
import com.bookmanageapp.bookmanagementapi.service.BookService
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/books")
@Validated
class BookController(
    private val bookService: BookService,
) {
    @PostMapping
    fun createBook(
        @Valid @RequestBody request: CreateBookRequest,
    ): ResponseEntity<Map<String, Long>> {
        val bookId = bookService.createBook(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapOf("id" to bookId))
    }

    @GetMapping
    fun getBooks(
        @RequestParam(defaultValue = "20")
        @Min(value = 1, message = "Limit must be at least 1")
        @Max(value = 100, message = "Limit must not exceed 100")
        limit: Int,
        @RequestParam(defaultValue = "0")
        @Min(value = 0, message = "Offset must not be negative")
        offset: Int,
    ): ResponseEntity<PagedResponse<BookResponse>> {
        val pagedResponse = bookService.getAllBooksWithOffsetPagination(limit, offset)
        return ResponseEntity.ok(pagedResponse)
    }
}
