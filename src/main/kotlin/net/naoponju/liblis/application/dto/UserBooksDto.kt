package net.naoponju.liblis.application.dto

import java.time.LocalDate
import java.util.UUID

data class UserBooksDto(
    val id: UUID?,
    val userId: UUID,
    val bookId: UUID,
    val status: String,
    val purchasePrice: Int?,
    val purchaseDate: LocalDate?, // DB: purchase_date DATE DEFAULT NULL
)

data class UserBooksForm(
    val bookId: UUID,
    val status: String,
    val purchasePrice: Int?,
    val purchaseYear: Int?,
    val purchaseMonth: Int?,
    val purchaseDay: Int?,
)

data class UserBooksUpdateForm(
    val bookId: UUID,
    val status: String,
    val purchasePrice: Int?,
    val purchaseDate: LocalDate?, // 更新時に未入力のケースも考慮
)
