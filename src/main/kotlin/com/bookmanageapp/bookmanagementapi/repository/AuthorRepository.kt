package com.bookmanageapp.bookmanagementapi.repository

import com.bookmanageapp.bookmanagementapi.domain.Author

interface AuthorRepository {
    fun save(author: Author): Author

    fun findById(id: Long): Author?

    fun existsById(id: Long): Boolean

    fun findByIds(ids: List<Long>): List<Author>
}
