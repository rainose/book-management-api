package com.bookmanageapp.bookmanagementapi.controller

import com.bookmanageapp.bookmanagementapi.dto.AuthorSummaryResponse
import com.bookmanageapp.bookmanagementapi.dto.CreateAuthorRequest
import com.bookmanageapp.bookmanagementapi.dto.PagedResponse
import com.bookmanageapp.bookmanagementapi.service.AuthorService
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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

    @GetMapping
    fun getAllAuthors(
        @RequestParam(defaultValue = "1") @Min(1) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int,
    ): ResponseEntity<PagedResponse<AuthorSummaryResponse>> {
        val pagedAuthors = authorService.getAllAuthorsWithPagination(page, size)
        val response =
            PagedResponse(
                content =
                    pagedAuthors.content.map { author ->
                        AuthorSummaryResponse(
                            id = author.id,
                            name = author.name,
                            birthDate = author.birthDate,
                        )
                    },
                pagination = pagedAuthors.pagination,
            )
        return ResponseEntity.ok(response)
    }
}
