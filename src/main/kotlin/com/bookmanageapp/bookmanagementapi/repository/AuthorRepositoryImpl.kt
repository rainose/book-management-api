package com.bookmanageapp.bookmanagementapi.repository

import com.bookmanageapp.bookmanagementapi.domain.Author
import com.bookmanageapp.jooq.tables.MAuthors.Companion.M_AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class AuthorRepositoryImpl(
    private val dslContext: DSLContext,
) : AuthorRepository {

    override fun findById(id: Long): Author? {
        return dslContext
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
    }

    override fun existsById(id: Long): Boolean {
        return (dslContext
            .selectCount()
            .from(M_AUTHORS)
            .where(M_AUTHORS.ID.eq(id))
            .fetchOne(0, Int::class.java) ?: 0) > 0
    }

    override fun findByIds(ids: List<Long>): List<Author> {
        return dslContext
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
    }

    override fun create(author: Author): Long {
        val now = LocalDateTime.now()
        return dslContext
            .insertInto(M_AUTHORS)
            .set(M_AUTHORS.NAME, author.name)
            .set(M_AUTHORS.BIRTH_DATE, author.birthDate)
            .set(M_AUTHORS.LOCK_NO, author.lockNo)
            .set(M_AUTHORS.CREATED_AT, now)
            .set(M_AUTHORS.CREATED_BY, "api_executer")
            .set(M_AUTHORS.UPDATED_AT, now)
            .set(M_AUTHORS.UPDATED_BY, "api_executer")
            .returningResult(M_AUTHORS.ID)
            .fetchOne()
            ?.value1()
            ?: throw IllegalStateException("Failed to create author and retrieve ID.")
    }

    override fun update(author: Author) {
        val now = LocalDateTime.now()
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
    }
}
