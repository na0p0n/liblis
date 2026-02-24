package net.naoponju.liblis.application.dto

data class GoogleBookDataDto(
    val title: String?,
    val authors: List<String>?,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val isbn10: String?,
    val isbn13: String?,
    val pageCount: Int?,
    val bookThumbnailURL: String?,
    val selfLink: String?,
)

// Google Books APIでISBNでの検索結果用のDTO
data class GoogleBookSearchResponseDto(
    val items: List<GoogleBookSearchResponseItemsDto>,
)

data class GoogleBookSearchResponseItemsDto(
    val selfLink: String,
    val volumeInfo: GoogleBookSearchResponseVolumeInfoDto?,
)

data class GoogleBookSearchResponseVolumeInfoDto(
    val title: String,
)

// selfLinkをAPIで叩いた結果のDto
data class GoogleBookDetailResponseItemDto(
    val selfLink: String?,
    val volumeInfo: GoogleBookDetailResponseVolumeInfoDto?,
)

data class GoogleBookDetailResponseVolumeInfoDto(
    val title: String?,
    val subtitle: String?,
    val authors: List<String>?,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val industryIdentifiers: List<GoogleBookDetailResponseIndustryIdentifiersDto>,
    val pageCount: Int?,
    val imageLinks: GoogleBookDetailResponseImageLinksDto?,
)

data class GoogleBookDetailResponseIndustryIdentifiersDto(
    val type: String,
    val identifier: String,
)

data class GoogleBookDetailResponseImageLinksDto(
    val smallThumbnail: String?,
    val thumbnail: String?,
)
