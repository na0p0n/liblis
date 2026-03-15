package net.naoponju.liblis.domain.entity

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class UserBooksEntity(
    val id: UUID,
    val userId: UUID,
    val bookId: UUID,
    val status: String,
    val purchasePrice: Int?,
    val purchaseDate: LocalDate,
    val isDeleted: Boolean = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
