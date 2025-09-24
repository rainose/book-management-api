package com.bookmanageapp.bookmanagementapi.config

import org.jooq.ExecuteContext
import org.jooq.impl.DefaultExecuteListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * jOOQのSQL実行ライフサイクルイベントをリッスンし、ログを出力するリスナー。
 *
 * @author nose yudai
 */
@Component
class JooqExecuteListener : DefaultExecuteListener() {
    private val logger = LoggerFactory.getLogger(JooqExecuteListener::class.java)

    /**
     * SQL実行開始時に呼び出され、実行するSQLをデバッグログに出力します。
     *
     * @param ctx 実行コンテキスト
     */
    override fun executeStart(ctx: ExecuteContext) {
        if (logger.isDebugEnabled) {
            logger.debug("Executing SQL: {}", ctx.sql())
        }
    }

    /**
     * SQL実行正常終了時に呼び出され、完了メッセージをデバッグログに出力します。
     *
     * @param ctx 実行コンテキスト
     */
    override fun executeEnd(ctx: ExecuteContext) {
        if (logger.isDebugEnabled) {
            logger.debug("SQL execution completed")
        }
    }

    /**
     * SQL実行中に例外が発生した場合に呼び出され、エラーログに失敗したSQLと例外情報を出力します。
     *
     * @param ctx 実行コンテキスト
     */
    override fun exception(ctx: ExecuteContext) {
        logger.error("SQL execution failed: {}", ctx.sql(), ctx.sqlException())
    }
}
