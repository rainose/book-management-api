package com.bookmanageapp.bookmanagementapi.util

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidBirthDateValidator::class])
annotation class ValidBirthDate(
    val message: String = "生年月日はクライアントのタイムゾーンにおける今日以前の日付である必要があります",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
