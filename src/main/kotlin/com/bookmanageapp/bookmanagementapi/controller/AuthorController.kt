package com.bookmanageapp.bookmanagementapi.controller

import com.bookmanageapp.bookmanagementapi.dto.AuthorResponse
import com.bookmanageapp.bookmanagementapi.dto.CreateAuthorRequest
import com.bookmanageapp.bookmanagementapi.service.AuthorService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
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
    ): ResponseEntity<AuthorResponse> {
        val author = authorService.createAuthor(request)
        val response =
            AuthorResponse(
                id = author.id!!,
                name = author.name,
                birthDate = author.birthDate,
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
