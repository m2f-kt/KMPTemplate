package com.m2f.core.config.server

import arrow.core.EitherNel
import arrow.core.NonEmptyList
import arrow.core.leftNel
import arrow.core.nonEmptyListOf
import arrow.core.right

interface InvalidField {
    val errors: NonEmptyList<String>
    val field: String
}

fun <A : InvalidField> toInvalidField(
    transform: (NonEmptyList<String>) -> A,
): (NonEmptyList<String>) -> NonEmptyList<A> = { nel -> nonEmptyListOf(transform(nel)) }

fun String.notBlank(): EitherNel<String, String> = if (isNotBlank()) right() else "Cannot be blank".leftNel()

fun String.minSize(size: Int): EitherNel<String, String> =
    if (length >= size) right() else "is too short (minimum is $size characters)".leftNel()

fun String.maxSize(size: Int): EitherNel<String, String> =
    if (length <= size) right() else "is too long (maximum is $size characters)".leftNel()
