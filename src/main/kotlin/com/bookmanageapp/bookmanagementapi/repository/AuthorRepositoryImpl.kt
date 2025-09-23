package com.bookmanageapp.bookmanagementapi.repository

import com.bookmanageapp.bookmanagementapi.domain.Author
import com.bookmanageapp.bookmanagementapi.domain.NewAuthor
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
                    id = requireNotNull(record.id),
                    name = record.name,
                    birthDate = record.birthDate,
                    lockNo = requireNotNull(record.lockNo),
                )
            }
    }

    override fun existsById(id: Long): Boolean {
        return (
            dslContext
                .selectCount()
                .from(M_AUTHORS)
                .where(M_AUTHORS.ID.eq(id))
                .fetchOne(0, Int::class.java) ?: 0
        ) > 0
    }

    override fun findByIds(ids: List<Long>): List<Author> {
        return dslContext
            .selectFrom(M_AUTHORS)
            .where(M_AUTHORS.ID.`in`(ids))
            .fetch()
            .map { record ->
                Author(
                    id = requireNotNull(record.id),
                    name = record.name,
                    birthDate = record.birthDate,
                    lockNo = requireNotNull(record.lockNo),
                )
            }
    }

    override fun create(author: NewAuthor): Long? {
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
    }

    override fun update(author: Author): Int {
        val now = LocalDateTime.now()
        return dslContext
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

    override fun findAll(): List<Author> {
        return dslContext
            .selectFrom(M_AUTHORS)
            .orderBy(M_AUTHORS.ID.asc())
            .fetch()
            .map { record ->
                Author(
                    id = requireNotNull(record.id),
                    name = record.name,
                    birthDate = record.birthDate,
                    lockNo = requireNotNull(record.lockNo),
                )
            }
    }

    override fun findAllWithPagination(
        page: Int,
        size: Int,
    ): Pair<List<Author>, Long> {
        val totalCount =
            dslContext
                .selectCount()
                .from(M_AUTHORS)
                .fetchOne(0, Long::class.java) ?: 0L

        val authors =
            dslContext
                .selectFrom(M_AUTHORS)
                .orderBy(M_AUTHORS.ID.asc())
                .limit(size)
                .offset((page - 1) * size)
                .fetch()
                .map { record ->
                    Author(
                        id = requireNotNull(record.id),
                        name = record.name,
                        birthDate = record.birthDate,
                        lockNo = requireNotNull(record.lockNo),
                    )
                }

        return Pair(authors, totalCount)
    }
}
