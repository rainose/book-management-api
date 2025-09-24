package com.bookmanageapp.bookmanagementapi.util

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidPublicationStatusCodeValidator::class])
annotation class ValidPublicationStatusCode(
    val message: String = "出版ステータスコードは '00' または '01' である必要があります",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
