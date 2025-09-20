package com.bookmanageapp.bookmanagementapi.service

import com.bookmanageapp.bookmanagementapi.domain.Author
import com.bookmanageapp.bookmanagementapi.dto.CreateAuthorRequest

interface AuthorService {
    fun createAuthor(request: CreateAuthorRequest): Author

    fun getAuthor(id: Long): Author

    fun validateAuthorExists(id: Long)

    fun validateAuthorsExist(ids: List<Long>)
}
