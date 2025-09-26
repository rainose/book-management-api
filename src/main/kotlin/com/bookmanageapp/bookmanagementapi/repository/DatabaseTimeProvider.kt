package com.bookmanageapp.bookmanagementapi.repository

import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DatabaseTimeProvider(
    private val dslContext: DSLContext,
) : TimeProvider {
    override fun getCurrentDate(clientTimeZone: String): LocalDate {
        return dslContext
            .select(DSL.currentDate())
            .fetchSingle()
            .value1()
            .toLocalDate()
    }
}
