package net.naoponju.liblis.application.dto

import java.time.LocalDate
import java.util.UUID

data class UserBooksDto(
    val id: UUID?,
    val userId: UUID,
    val bookId: UUID,
    val status: String,
    val purchasePrice: Int?,
    val purchaseDate: LocalDate?,
)

data class UserBooksForm(
    val userId: UUID,
    val bookId: UUID,
    val status: String,
    val purchasePrice: Int?,
    val purchaseYear: Int?,
    val purchaseMonth: Int?,
    val purchaseDay: Int?,
)
