package com.bookmanageapp.bookmanagementapi.repository

import com.bookmanageapp.bookmanagementapi.domain.Author
import com.bookmanageapp.bookmanagementapi.exception.DatabaseException
import com.bookmanageapp.jooq.tables.MAuthors.Companion.M_AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class AuthorRepositoryImpl(
    private val dslContext: DSLContext,
) : AuthorRepository {
    override fun save(author: Author): Author {
        return try {
            if (author.id == null) {
                create(author)
            } else {
                update(author)
            }
        } catch (e: Exception) {
            throw DatabaseException("Failed to save author: ${e.message}", e)
        }
    }

    override fun findById(id: Long): Author? {
        return try {
            dslContext
                .selectFrom(M_AUTHORS)
                .where(M_AUTHORS.ID.eq(id))
                .fetchOne()
                ?.let { record ->
                    Author(
                        id = record.id,
                        name = record.name,
                        birthDate = record.birthDate,
                        lockNo = record.lockNo ?: 1,
                    )
                }
        } catch (e: Exception) {
            throw DatabaseException("Failed to find author by ID $id: ${e.message}", e)
        }
    }

    override fun existsById(id: Long): Boolean {
        return try {
            dslContext
                .selectCount()
                .from(M_AUTHORS)
                .where(M_AUTHORS.ID.eq(id))
                .fetchOne(0, Int::class.java) ?: 0 > 0
        } catch (e: Exception) {
            throw DatabaseException("Failed to check author existence by ID $id: ${e.message}", e)
        }
    }

    override fun findByIds(ids: List<Long>): List<Author> {
        return try {
            if (ids.isEmpty()) return emptyList()

            dslContext
                .selectFrom(M_AUTHORS)
                .where(M_AUTHORS.ID.`in`(ids))
                .fetch()
                .map { record ->
                    Author(
                        id = record.id,
                        name = record.name,
                        birthDate = record.birthDate,
                        lockNo = record.lockNo ?: 1,
                    )
                }
        } catch (e: Exception) {
            throw DatabaseException("Failed to find authors by IDs ${ids.joinToString()}: ${e.message}", e)
        }
    }

    private fun create(author: Author): Author {
        val now = LocalDateTime.now()
        val record =
            dslContext
                .insertInto(M_AUTHORS)
                .set(M_AUTHORS.NAME, author.name)
                .set(M_AUTHORS.BIRTH_DATE, author.birthDate)
                .set(M_AUTHORS.LOCK_NO, author.lockNo)
                .set(M_AUTHORS.CREATED_AT, now)
                .set(M_AUTHORS.CREATED_BY, "api_executer")
                .set(M_AUTHORS.UPDATED_AT, now)
                .set(M_AUTHORS.UPDATED_BY, "api_executer")
                .returning()
                .fetchOne()
                ?: throw DatabaseException("Failed to create author - no record returned")

        return Author(
            id = record.id,
            name = record.name,
            birthDate = record.birthDate,
            lockNo = record.lockNo ?: 1,
        )
    }

    private fun update(author: Author): Author {
        val now = LocalDateTime.now()
        val updatedCount =
            dslContext
                .update(M_AUTHORS)
                .set(M_AUTHORS.NAME, author.name)
                .set(M_AUTHORS.BIRTH_DATE, author.birthDate)
                .set(M_AUTHORS.LOCK_NO, author.lockNo + 1)
                .set(M_AUTHORS.UPDATED_AT, now)
                .set(M_AUTHORS.UPDATED_BY, "api_executer")
                .where(
                    M_AUTHORS.ID.eq(author.id)
                        .and(M_AUTHORS.LOCK_NO.eq(author.lockNo)),
                )
                .execute()

        if (updatedCount == 0) {
            throw DatabaseException("Failed to update author - no rows affected or optimistic lock conflict")
        }

        return author.copy(lockNo = author.lockNo + 1)
    }
}
