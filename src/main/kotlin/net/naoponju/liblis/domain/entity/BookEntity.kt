package net.naoponju.liblis.domain.entity

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class BookEntity(
    val id: UUID?,
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
    val registrationCount: Int,
    val isSearchedNDL: Boolean = false,
    val ndlUrl: String?,
    val isSearchedGoogle: Boolean = false,
    val googleUrl: String?,
    val isSearchedRakuten: Boolean = false,
    val rakutenItemUrl: String? = null,
    val rakutenAffiliateUrl: String? = null,
    val createdAt: LocalDateTime? = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = LocalDateTime.now(),
)