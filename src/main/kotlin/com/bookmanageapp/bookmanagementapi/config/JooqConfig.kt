package com.bookmanageapp.bookmanagementapi.config

import org.jooq.ExecuteListenerProvider
import org.jooq.impl.DefaultExecuteListenerProvider
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
class JooqConfig {
    @Bean
    fun jooqConfigurationCustomizer(): DefaultConfigurationCustomizer {
        return DefaultConfigurationCustomizer { configuration ->
            configuration.set(executeListenerProvider())
        }
    }

    @Bean
    fun executeListenerProvider(): ExecuteListenerProvider {
        return DefaultExecuteListenerProvider(JooqExecuteListener())
    }
}
