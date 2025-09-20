package com.bookmanageapp.bookmanagementapi.config

import org.jooq.ExecuteContext
import org.jooq.impl.DefaultExecuteListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class JooqExecuteListener : DefaultExecuteListener() {
    private val logger = LoggerFactory.getLogger(JooqExecuteListener::class.java)

    override fun executeStart(ctx: ExecuteContext) {
        if (logger.isDebugEnabled) {
            logger.debug("Executing SQL: {}", ctx.sql())
        }
    }

    override fun executeEnd(ctx: ExecuteContext) {
        if (logger.isDebugEnabled) {
            logger.debug("SQL execution completed")
        }
    }

    override fun exception(ctx: ExecuteContext) {
        logger.error("SQL execution failed: {}", ctx.sql(), ctx.sqlException())
    }
}
