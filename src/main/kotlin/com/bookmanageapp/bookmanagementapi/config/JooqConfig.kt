package com.bookmanageapp.bookmanagementapi.config

import org.jooq.ExecuteListenerProvider
import org.jooq.impl.DefaultExecuteListenerProvider
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * jOOQの設定クラス。
 *
 * トランザクション管理を有効にし、jOOQのカスタム設定を提供します。
 *
 * @author nose yudai
 */
@Configuration
@EnableTransactionManagement
class JooqConfig {
    /**
     * jOOQのデフォルト設定をカスタマイズするためのBeanを生成します。
     *
     * SQL実行リスナーをjOOQのコンフィグレーションに設定します。
     *
     * @return [DefaultConfigurationCustomizer] カスタマイズされたjOOQ設定
     */
    @Bean
    fun jooqConfigurationCustomizer(): DefaultConfigurationCustomizer {
        return DefaultConfigurationCustomizer { configuration ->
            configuration.set(executeListenerProvider())
        }
    }

    /**
     * SQL実行リスナーを提供するためのBeanを生成します。
     *
     * [JooqExecuteListener]を[DefaultExecuteListenerProvider]にラップして提供します。
     *
     * @return [ExecuteListenerProvider] SQL実行リスナープロバイダー
     */
    @Bean
    fun executeListenerProvider(): ExecuteListenerProvider {
        return DefaultExecuteListenerProvider(JooqExecuteListener())
    }
}
