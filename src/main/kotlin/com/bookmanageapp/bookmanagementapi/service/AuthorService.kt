package com.bookmanageapp.bookmanagementapi.service

import com.bookmanageapp.bookmanagementapi.domain.Author
import com.bookmanageapp.bookmanagementapi.domain.NewAuthor
import com.bookmanageapp.bookmanagementapi.dto.CreateAuthorRequest
import com.bookmanageapp.bookmanagementapi.dto.PagedResponse
import com.bookmanageapp.bookmanagementapi.dto.PaginationInfo
import com.bookmanageapp.bookmanagementapi.dto.UpdateAuthorRequest
import com.bookmanageapp.bookmanagementapi.exception.AuthorNotFoundException
import com.bookmanageapp.bookmanagementapi.exception.OptimisticLockException
import com.bookmanageapp.bookmanagementapi.repository.AuthorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
    fun getAllAuthors(): List<Author> {
        return authorRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getAllAuthorsWithPagination(
        page: Int,
        size: Int,
    ): PagedResponse<Author> {
        val (authors, totalCount) = authorRepository.findAllWithPagination(page, size)
        val paginationInfo = PaginationInfo.fromPageNumber(page, size, totalCount)

        return PagedResponse(
            content = authors,
            pagination = paginationInfo,
        )
    }

    fun updateAuthor(
        id: Long,
        request: UpdateAuthorRequest,
    ) {
        if (!authorRepository.existsById(id)) {
            throw AuthorNotFoundException(id)
        }

        val updatedAuthor =
            Author(
                id = id,
                name = request.name.trim(),
                birthDate = request.birthDate,
                lockNo = request.lockNo,
            )

        val updatedRows = authorRepository.update(updatedAuthor)
        if (updatedRows == 0) {
            throw OptimisticLockException()
        }
    }
}
