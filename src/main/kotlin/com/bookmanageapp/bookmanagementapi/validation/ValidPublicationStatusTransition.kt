package com.bookmanageapp.bookmanagementapi.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PublicationStatusTransitionValidator::class])
@MustBeDocumented
annotation class ValidPublicationStatusTransition(
    val message: String = "Invalid publication status transition",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class PublicationStatusTransitionValidator : ConstraintValidator<ValidPublicationStatusTransition, Any> {
    override fun isValid(
        value: Any?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        return true
    }
}
