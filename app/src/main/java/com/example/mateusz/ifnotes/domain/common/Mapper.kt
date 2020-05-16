package com.example.mateusz.ifnotes.domain.common

import com.google.common.base.Optional

abstract class Mapper<E, T> {
    abstract fun mapFrom(from: E): T

    fun mapOptional(from: Optional<E>): Optional<T> {
        return if (from.isPresent) Optional.of(mapFrom(from.get())) else Optional.absent<T>()
    }
}
