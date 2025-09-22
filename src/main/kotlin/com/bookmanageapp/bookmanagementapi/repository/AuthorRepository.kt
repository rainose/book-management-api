package com.bookmanageapp.bookmanagementapi.repository

import com.bookmanageapp.bookmanagementapi.domain.Author

interface AuthorRepository {
    fun create(author: Author): Long

    fun update(author: Author)

    fun findById(id: Long): Author?

    fun existsById(id: Long): Boolean

    fun findByIds(ids: List<Long>): List<Author>
}
