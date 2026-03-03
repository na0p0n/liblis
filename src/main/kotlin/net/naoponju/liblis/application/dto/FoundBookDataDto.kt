package net.naoponju.liblis.application.dto

import java.time.LocalDate

data class FoundBookDataDto(
    val title: String?,
    val author: List<String>?,
    val publisher: String?,
    val publishDate: LocalDate?,
    val pages: Int?,
    val description: String?,
    val isbn10: String?,
    val isbn13: String?,
    val listPrice: Int?,
    val category: String?,
    val thumbnailUrl: String?,
    val registrationCount: Int,
    val isSearchedNDL: Boolean = false,
    val ndlUrl: String?,
    val isSearchedGoogle: Boolean = false,
    val googleUrl: String?,
)
