package com.bookmanageapp.bookmanagementapi.controller

import com.bookmanageapp.bookmanagementapi.dto.CreateBookRequest
import com.bookmanageapp.bookmanagementapi.service.BookService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/books")
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
}
