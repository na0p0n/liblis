package net.naoponju.liblis.application.dto

import java.time.LocalDate
import java.util.UUID

data class LibraryBookDto(
    val userBooksId: UUID,
    val bookId: UUID?,
    val title: String?,
    val titleKana: String? = null,
    val subTitle: String? = null,
    val subTitleKana: String? = null,
    val author: List<String>?,
    val publisher: String?,
    val bookSize: Int? = null,
    val publishDate: LocalDate?,
    val pages: Int?,
    val description: String?,
    val isbn10: String?,
    val isbn13: String?,
    val listPrice: Int?,
    val category: String?,
    val smallThumbnailUrl: String? = null,
    val thumbnailUrl: String?,
    val largeThumbnailUrl: String? = null,
    val status: String,
    val purchasePrice: Int?,
    val purchaseDate: LocalDate?,
)
