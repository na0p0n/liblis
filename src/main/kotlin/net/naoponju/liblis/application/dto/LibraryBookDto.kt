package net.naoponju.liblis.application.dto

import java.time.LocalDate
import java.util.UUID

data class LibraryBookDto(
    val userBooksId: UUID,
    val bookId: UUID,
    val title: String?,
    val author: List<String>?,
    val publisher: String?,
    val publishDate: LocalDate?,
    val pages: Int?,
    val isbn10: String?,
    val isbn13: String?,
    val thumbnailUrl: String?,
    val status: String,
    val purchasePrice: Int?,
    val purchaseDate: LocalDate?,
)
