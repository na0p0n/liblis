package net.naoponju.liblis.infra.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.naoponju.liblis.application.dto.GoogleBookDataDto
import net.naoponju.liblis.application.dto.GoogleBookDetailResponseItemDto
import net.naoponju.liblis.application.dto.GoogleBookSearchResponseDto
import net.naoponju.liblis.common.config.LoggingAspect
import net.naoponju.liblis.common.exception.ApiKeyNotFoundException
import net.naoponju.liblis.common.exception.BookNotFoundException
import net.naoponju.liblis.common.exception.RemoteApiServiceException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URL

@Component
class GoogleBooksApiClient(
    @Value("\${google.api.key}")
    private val apiKey: String
) {

    // Spring標準の変換機能に頼らず、自前でJacksonを構築（干渉を避けるため）
    private val objectMapper = ObjectMapper()
        .registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun fetchBookData(isbn: String): GoogleBookDataDto {
        if (apiKey == "default_key_if_needed") {
            throw ApiKeyNotFoundException("APIキーが設定されていません。")
        }

        try {
            // 1. 最初の検索リクエスト (ISBNで検索)
            val searchUrl = "https://www.googleapis.com/books/v1/volumes?q=isbn:$isbn&key=$apiKey"
            logger.info("Search Request URI: $searchUrl")

            val searchJson = URL(searchUrl).readText()
            val searchResponse = objectMapper.readValue(searchJson, GoogleBookSearchResponseDto::class.java)

            // 該当するアイテムがない場合は例外
            val firstItem = searchResponse?.items?.firstOrNull()
                ?: throw BookNotFoundException("対象のISBNの本がGoogle Booksで見つかりません。 ISBN: $isbn")

            // 2. 詳細データのリクエスト (selfLink)
            // selfLink にも APIキーを付与しないと制限がかかる場合があるため、URLを調整
            val rawSelfLink = firstItem.selfLink
            val detailUrl = if (rawSelfLink.contains("?")) "$rawSelfLink" else "$rawSelfLink"

            logger.info("Detail Request URI: $detailUrl")

            val detailJson = URL(detailUrl).readText()
            val detailResponse = objectMapper.readValue(detailJson, GoogleBookDetailResponseItemDto::class.java)

            // 3. データの抽出
            val volumeInfo = detailResponse.volumeInfo
            val isbn10 = volumeInfo?.industryIdentifiers?.find { it.type == "ISBN_10" }?.identifier
            val isbn13 = volumeInfo?.industryIdentifiers?.find { it.type == "ISBN_13" }?.identifier
            val bookThumbnailUrl = volumeInfo?.imageLinks?.thumbnail ?: volumeInfo?.imageLinks?.smallThumbnail
            val bookTitle = firstItem.volumeInfo?.title

            return GoogleBookDataDto(
                title = bookTitle,
                authors = volumeInfo?.authors,
                publisher = volumeInfo?.publisher,
                publishedDate = volumeInfo?.publishedDate,
                description = volumeInfo?.description,
                isbn10 = isbn10,
                isbn13 = isbn13,
                pageCount = volumeInfo?.pageCount,
                bookThumbnailURL = bookThumbnailUrl,
                selfLink = detailResponse.selfLink
            )

        } catch (e: BookNotFoundException) {
            throw e
        } catch (e: Exception) {
            logger.error("Google Books API 連携中にエラーが発生しました: ${e.message}", e)
            throw RemoteApiServiceException("Google Books API側でエラーが発生しました")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LoggingAspect::class.java)
    }
}