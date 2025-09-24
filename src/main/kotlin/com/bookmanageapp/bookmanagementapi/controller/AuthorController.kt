package com.bookmanageapp.bookmanagementapi.controller

import com.bookmanageapp.bookmanagementapi.dto.AuthorBooksResponse
import com.bookmanageapp.bookmanagementapi.dto.CreateAuthorRequest
import com.bookmanageapp.bookmanagementapi.dto.UpdateAuthorRequest
import com.bookmanageapp.bookmanagementapi.service.AuthorService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/authors")
class AuthorController(
    private val authorService: AuthorService,
) {
    @PostMapping
    fun createAuthor(
        @Valid @RequestBody request: CreateAuthorRequest,
    ): ResponseEntity<Map<String, Long>> {
        val authorId = authorService.createAuthor(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapOf("id" to authorId))
    }

    @PutMapping("/{id}")
    fun updateAuthor(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateAuthorRequest,
    ): ResponseEntity<Void> {
        authorService.updateAuthor(id, request)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/books")
    fun getAuthorBooks(
        @PathVariable id: Long,
    ): ResponseEntity<AuthorBooksResponse> {
        val response = authorService.getAuthorBooks(id)
        return ResponseEntity.ok(response)
    }
}
