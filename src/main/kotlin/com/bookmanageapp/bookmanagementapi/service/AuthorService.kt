package com.bookmanageapp.bookmanagementapi.service

import com.bookmanageapp.bookmanagementapi.domain.Author
import com.bookmanageapp.bookmanagementapi.domain.NewAuthor
import com.bookmanageapp.bookmanagementapi.dto.CreateAuthorRequest
import com.bookmanageapp.bookmanagementapi.dto.PagedResponse
import com.bookmanageapp.bookmanagementapi.dto.PaginationInfo
import com.bookmanageapp.bookmanagementapi.exception.AuthorNotFoundException
import com.bookmanageapp.bookmanagementapi.exception.AuthorsNotFoundException
import com.bookmanageapp.bookmanagementapi.repository.AuthorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.ceil

@Service
@Transactional
class AuthorService(
    private val authorRepository: AuthorRepository,
) {
    fun createAuthor(request: CreateAuthorRequest): Long {
        val author =
            NewAuthor(
                name = request.name.trim(),
                birthDate = request.birthDate,
            )
        val authorId = authorRepository.create(author)
        return requireNotNull(authorId)
    }

    @Transactional(readOnly = true)
    fun getAuthor(id: Long): Author {
        return authorRepository.findById(id)
            ?: throw AuthorNotFoundException(id)
    }

    @Transactional(readOnly = true)
    fun validateAuthorExists(id: Long) {
        if (!authorRepository.existsById(id)) {
            throw AuthorNotFoundException(id)
        }
    }

    @Transactional(readOnly = true)
    fun validateAuthorsExist(ids: List<Long>) {
        if (ids.isEmpty()) return

        val existingAuthors = authorRepository.findByIds(ids)
        val existingIds = existingAuthors.map { it.id }.toSet()
        val missingIds = ids.filter { it !in existingIds }

        if (missingIds.isNotEmpty()) {
            throw AuthorsNotFoundException(missingIds)
        }
    }

    @Transactional(readOnly = true)
    fun getAllAuthors(): List<Author> {
        return authorRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getAllAuthorsWithPagination(
        page: Int,
        size: Int,
    ): PagedResponse<Author> {
        val (authors, totalCount) = authorRepository.findAllWithPagination(page, size)
        val totalPages = if (totalCount > 0) ceil(totalCount.toDouble() / size).toInt() else 0

        val paginationInfo =
            PaginationInfo(
                currentPage = page,
                pageSize = size,
                totalElements = totalCount,
                totalPages = totalPages,
                hasNext = page < totalPages,
                hasPrevious = page > 1,
            )

        return PagedResponse(
            content = authors,
            pagination = paginationInfo,
        )
    }
}
