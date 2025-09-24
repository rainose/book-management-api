package com.bookmanageapp.bookmanagementapi.repository

import com.bookmanageapp.bookmanagementapi.domain.Author
import com.bookmanageapp.bookmanagementapi.domain.NewAuthor

interface AuthorRepository {
    fun create(author: NewAuthor): Long?

    fun update(author: Author): Int

    fun findById(id: Long): Author?

    fun existsById(id: Long): Boolean

    fun countByIds(ids: List<Long>): Int
}
