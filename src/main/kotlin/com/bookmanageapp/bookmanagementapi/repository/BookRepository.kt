package com.bookmanageapp.bookmanagementapi.repository

import com.bookmanageapp.bookmanagementapi.domain.Book
import com.bookmanageapp.bookmanagementapi.domain.NewBook

interface BookRepository {
    fun create(book: NewBook): Long?

    fun update(book: Book): Int

    fun findById(id: Long): Book?

    fun existsById(id: Long): Boolean

    fun findAll(): List<Book>

    fun findAllWithPagination(
        page: Int,
        size: Int,
    ): Pair<List<Book>, Long>

    fun findByAuthorId(authorId: Long): List<Book>
}
