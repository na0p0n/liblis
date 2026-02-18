package net.naoponju.liblis.entity

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class BookEntity(
    val id: UUID,
    val title: String,
    val author: String?,
    val publisher: String?,
    val publishDate: LocalDate?,
    val pages: Int?,
    val description: String?,
    val isbn: String?,
    val listPrice: Int?,
    val category: String?,
    val thumbnailUrl: String?,
    val registrationCount: Int,
    val isSearchedNDL: Boolean = false,
    val ndlUrl: String?,
    val isSearchedGoogle: Boolean = false,
    val googleUrl: String?,
    val createdAt: LocalDateTime? = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = LocalDateTime.now()
)
