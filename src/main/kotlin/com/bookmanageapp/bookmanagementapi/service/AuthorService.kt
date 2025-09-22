package com.bookmanageapp.bookmanagementapi.service

import com.bookmanageapp.bookmanagementapi.domain.Author
import com.bookmanageapp.bookmanagementapi.dto.CreateAuthorRequest
import com.bookmanageapp.bookmanagementapi.exception.AuthorNotFoundException
import com.bookmanageapp.bookmanagementapi.exception.AuthorsNotFoundException
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
            Author(
                id = null,
                name = request.name.trim(),
                birthDate = request.birthDate,
            )

        return authorRepository.create(author)
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
        val existingIds = existingAuthors.mapNotNull { it.id }.toSet()
        val missingIds = ids.filter { it !in existingIds }

        if (missingIds.isNotEmpty()) {
            throw AuthorsNotFoundException(missingIds)
        }
    }
}
