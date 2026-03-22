package net.naoponju.liblis.infra.api

import net.naoponju.liblis.domain.entity.BookEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDate
import java.util.UUID

@Component
class RakutenBooksApiClient(
    @Value("\${rakuten.api.application-id}") private val applicationId: String,
    @Value("\${rakuten.api.key}") private val accessKey: String,
    @Value("\${rakuten.api.affiliate-id:}") private val affiliateId: String,
    @Value("\${rakuten.api.site-url}") private val siteUrl: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restTemplate = RestTemplate(HttpComponentsClientHttpRequestFactory())

    companion object {
        private const val API_URL =
            "https://openapi.rakuten.co.jp/services/api/BooksBook/Search/20170404"
    }

    fun findByIsbn(isbn: String): BookEntity? {
        val url = UriComponentsBuilder.fromHttpUrl(API_URL)
            .queryParam("applicationId", applicationId)
            .queryParam("accessKey", accessKey)
            .queryParam("isbn", isbn)
            .queryParam("hits", 1)
            .queryParam("format", "json")
            .queryParam("formatVersion", 2)
            .apply { if (affiliateId.isNotBlank()) queryParam("affiliateId", affiliateId) }
            .toUriString()
        log.info("URL: $url")

        // ★ getForObject() ではなく exchange() を使う（Referer/Origin がないと403）
        val headers = HttpHeaders().apply {
            set("Referer", siteUrl)
            set("Origin", siteUrl)
            set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0")
        }
        log.info("header: $headers")

        return try {
            @Suppress("UNCHECKED_CAST")
            val response = restTemplate
                .exchange(url, HttpMethod.GET, HttpEntity<Void>(null, headers), Map::class.java)
                .body as? Map<String, Any> ?: return null

            val count = (response["count"] as? Int) ?: 0
            if (count == 0) { log.debug("楽天ブックスAPI: 未ヒット isbn=$isbn"); return null }

            @Suppress("UNCHECKED_CAST")
            val item = (response["Items"] as? List<Map<String, Any>>)?.firstOrNull() ?: return null
            mapToEntity(item)
        } catch (e: Exception) {
            log.warn("楽天ブックスAPI呼び出し失敗 isbn=$isbn : ${e.message}")
            null
        }
    }

    private fun mapToEntity(item: Map<String, Any>): BookEntity {
        val isbn = item["isbn"] as? String ?: ""
        val isbn10 = if (isbn.length == 10) isbn else null
        val isbn13 = if (isbn.length == 13) isbn else null

        val authors = (item["author"] as? String)
            ?.split("/")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        val largeImageUrl = (item["largeImageUrl"] as? String)?.takeIf { it.isNotEmpty() }
        val mediumImageUrl = (item["mediumImageUrl"] as? String)?.takeIf { it.isNotEmpty() }
        val smallImageUrl = (item["smallImageUrl"] as? String)?.takeIf { it.isNotEmpty() }

        return BookEntity(
            id = UUID.randomUUID(),
            title = item["title"] as? String,
            titleKana = item["titleKana"] as? String,       // ★追加
            subTitle = item["subTitle"] as? String,         // ★追加
            subTitleKana = item["subTitleKana"] as? String, // ★追加
            author = authors,
            publisher = item["publisherName"] as? String,
            bookSize = (item["size"] as? Int),              // ★追加
            publishDate = parsePublishDate(item["salesDate"] as? String),
            pages = null,
            description = (item["itemCaption"] as? String)?.takeIf { it.isNotEmpty() },
            isbn10 = isbn10,
            isbn13 = isbn13,
            listPrice = (item["itemPrice"] as? Int),        // ★listPrice→itemPrice（listPriceは常に0）
            category = item["booksGenreId"] as? String,
            smallThumbnailUrl = smallImageUrl,
            thumbnailUrl = mediumImageUrl,
            largeThumbnailUrl = largeImageUrl ?: mediumImageUrl,
            registrationCount = 0,
            isSearchedNDL = false,
            ndlUrl = null,
            isSearchedGoogle = false,
            googleUrl = null,
            isSearchedRakuten = true,
            rakutenItemUrl = item["itemUrl"] as? String,
            rakutenAffiliateUrl = (item["affiliateUrl"] as? String)?.takeIf { it.isNotEmpty() },
        )
    }

    /**
     * salesDate パース。公式仕様上「上旬・中旬・下旬・頃」等の自然語も来うるため、
     * 正規表現で数値部分のみ抽出してパースし、失敗時は null を返す。
     */
    private fun parsePublishDate(salesDate: String?): LocalDate? {
        if (salesDate.isNullOrBlank()) return null

        // 数値ベースの年月日を正規表現で抽出（「上旬」等の自然語を除去）
        val yearMonthDay = Regex("""(\d{4})年(\d{2})月(\d{2})日""").find(salesDate)
        if (yearMonthDay != null) {
            return runCatching {
                LocalDate.of(
                    yearMonthDay.groupValues[1].toInt(),
                    yearMonthDay.groupValues[2].toInt(),
                    yearMonthDay.groupValues[3].toInt(),
                )
            }.getOrNull()
        }

        val yearMonth = Regex("""(\d{4})年(\d{2})月""").find(salesDate)
        if (yearMonth != null) {
            return runCatching {
                LocalDate.of(
                    yearMonth.groupValues[1].toInt(),
                    yearMonth.groupValues[2].toInt(),
                    1,
                )
            }.getOrNull()
        }

        val yearOnly = Regex("""(\d{4})年""").find(salesDate)
        if (yearOnly != null) {
            return runCatching {
                LocalDate.of(yearOnly.groupValues[1].toInt(), 1, 1)
            }.getOrNull()
        }

        log.debug("salesDate パース失敗（NULLで登録）: $salesDate")
        return null
    }
}