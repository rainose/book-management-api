package com.bookmanageapp.bookmanagementapi.util

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidPublicationStatusCodeValidator::class])
annotation class ValidPublicationStatusCode(
    val message: String = "Publication status code must be '00' or '01'",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
