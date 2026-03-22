package net.naoponju.liblis.infra.api

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.lang.reflect.Method
import java.time.LocalDate

class RakutenBooksApiClientTest {

    // ─── ヘルパー ─────────────────────────────────────────────────────────────

    private fun createClient(restTemplate: RestTemplate = mockk()): RakutenBooksApiClient =
        RakutenBooksApiClient(
            applicationId = "test-app-id",
            accessKey = "test-access-key",
            affiliateId = "",
            siteUrl = "https://example.com",
            restTemplate = restTemplate,
        )

    private fun mockRestTemplate(responseBody: Map<String, Any>): RestTemplate {
        val rt = mockk<RestTemplate>()
        @Suppress("UNCHECKED_CAST")
        every {
            rt.exchange(any<String>(), HttpMethod.GET, any(), Map::class.java)
        } returns ResponseEntity.ok(responseBody as Map<*, *>)
        return rt
    }

    private fun getParsePublishDate(): Method {
        val method = RakutenBooksApiClient::class.java.getDeclaredMethod("parsePublishDate", String::class.java)
        method.isAccessible = true
        return method
    }

    private fun callParsePublishDate(client: RakutenBooksApiClient, salesDate: String?): LocalDate? {
        @Suppress("UNCHECKED_CAST")
        return getParsePublishDate().invoke(client, salesDate) as? LocalDate
    }

    // ─── parsePublishDate テスト ───────────────────────────────────────────────

    @Test
    @DisplayName("salesDateパース_正常系_null入力はnullを返す")
    fun parsePublishDateNullInput() {
        Assertions.assertNull(callParsePublishDate(createClient(), null))
    }

    @Test
    @DisplayName("salesDateパース_正常系_空文字はnullを返す")
    fun parsePublishDateEmptyString() {
        Assertions.assertNull(callParsePublishDate(createClient(), ""))
    }

    @Test
    @DisplayName("salesDateパース_正常系_空白のみはnullを返す")
    fun parsePublishDateBlankString() {
        Assertions.assertNull(callParsePublishDate(createClient(), "   "))
    }

    @Test
    @DisplayName("salesDateパース_正常系_年月日形式（yyyy年MM月dd日）")
    fun parsePublishDateYearMonthDay() {
        Assertions.assertEquals(
            LocalDate.of(2024, 3, 15),
            callParsePublishDate(createClient(), "2024年03月15日"),
        )
    }

    @Test
    @DisplayName("salesDateパース_正常系_年月形式（yyyy年MM月）は1日になる")
    fun parsePublishDateYearMonth() {
        Assertions.assertEquals(
            LocalDate.of(2024, 3, 1),
            callParsePublishDate(createClient(), "2024年03月"),
        )
    }

    @Test
    @DisplayName("salesDateパース_正常系_年のみ形式（yyyy年）は1月1日になる")
    fun parsePublishDateYearOnly() {
        Assertions.assertEquals(
            LocalDate.of(2024, 1, 1),
            callParsePublishDate(createClient(), "2024年"),
        )
    }

    @Test
    @DisplayName("salesDateパース_正常系_上旬付きの年月（yyyy年MM月上旬）は1日になる")
    fun parsePublishDateYearMonthJoJun() {
        Assertions.assertEquals(
            LocalDate.of(2024, 3, 1),
            callParsePublishDate(createClient(), "2024年03月上旬"),
        )
    }

    @Test
    @DisplayName("salesDateパース_正常系_中旬付きの年月（yyyy年MM月中旬）は1日になる")
    fun parsePublishDateYearMonthChuJun() {
        Assertions.assertEquals(
            LocalDate.of(2024, 6, 1),
            callParsePublishDate(createClient(), "2024年06月中旬"),
        )
    }

    @Test
    @DisplayName("salesDateパース_正常系_下旬付きの年月（yyyy年MM月下旬）は1日になる")
    fun parsePublishDateYearMonthKaJun() {
        Assertions.assertEquals(
            LocalDate.of(2023, 12, 1),
            callParsePublishDate(createClient(), "2023年12月下旬"),
        )
    }

    @Test
    @DisplayName("salesDateパース_正常系_頃付きの年（yyyy年頃）は1月1日になる")
    fun parsePublishDateYearAround() {
        Assertions.assertEquals(
            LocalDate.of(2020, 1, 1),
            callParsePublishDate(createClient(), "2020年頃"),
        )
    }

    @Test
    @DisplayName("salesDateパース_正常系_数字のない文字列はnullを返す")
    fun parsePublishDateNoNumbers() {
        Assertions.assertNull(callParsePublishDate(createClient(), "未定"))
    }

    @Test
    @DisplayName("salesDateパース_正常系_数字はあるが年月日パターンなしはnullを返す")
    fun parsePublishDateNumbersButNoYMD() {
        Assertions.assertNull(callParsePublishDate(createClient(), "1234567890"))
    }

    @Test
    @DisplayName("salesDateパース_境界値_不正な月（13月）はnullを返す")
    fun parsePublishDateInvalidMonth() {
        Assertions.assertNull(callParsePublishDate(createClient(), "2024年13月"))
    }

    @Test
    @DisplayName("salesDateパース_境界値_不正な日（32日）はnullを返す")
    fun parsePublishDateInvalidDay() {
        Assertions.assertNull(callParsePublishDate(createClient(), "2024年01月32日"))
    }

    @Test
    @DisplayName("salesDateパース_境界値_閏年の2月29日は正常パース")
    fun parsePublishDateLeapYearFeb29() {
        Assertions.assertEquals(
            LocalDate.of(2024, 2, 29),
            callParsePublishDate(createClient(), "2024年02月29日"),
        )
    }

    @Test
    @DisplayName("salesDateパース_境界値_非閏年の2月29日はnullを返す")
    fun parsePublishDateNonLeapYearFeb29() {
        Assertions.assertNull(callParsePublishDate(createClient(), "2023年02月29日"))
    }

    // ─── findByIsbn テスト ───────────────────────────────────────────────────

    @Test
    @DisplayName("findByIsbn_正常系_APIが書籍を返す場合BookEntityを返す")
    fun findByIsbnSuccess01() {
        val rt = mockRestTemplate(
            mapOf(
                "count" to 1,
                "Items" to listOf(
                    mapOf(
                        "title" to "テスト書籍",
                        "titleKana" to "テストショセキ",
                        "subTitle" to "",
                        "subTitleKana" to "",
                        "author" to "著者A / 著者B",
                        "publisherName" to "出版社テスト",
                        "isbn" to "9784000000001",
                        "salesDate" to "2024年03月15日",
                        "itemPrice" to 1500,
                        "booksGenreId" to "001004008",
                        "itemCaption" to "テスト本の説明",
                        "smallImageUrl" to "https://example.com/small.jpg",
                        "mediumImageUrl" to "https://example.com/medium.jpg",
                        "largeImageUrl" to "https://example.com/large.jpg",
                        "itemUrl" to "https://books.rakuten.co.jp/rb/12345/",
                        "affiliateUrl" to "",
                        "size" to 1,
                    ),
                ),
            ),
        )

        val result = createClient(rt).findByIsbn("9784000000001")

        Assertions.assertNotNull(result)
        Assertions.assertEquals("テスト書籍", result!!.title)
        Assertions.assertEquals("テストショセキ", result.titleKana)
        Assertions.assertEquals("出版社テスト", result.publisher)
        Assertions.assertEquals("9784000000001", result.isbn13)
        Assertions.assertNull(result.isbn10)
        Assertions.assertEquals(listOf("著者A", "著者B"), result.author)
        Assertions.assertEquals(LocalDate.of(2024, 3, 15), result.publishDate)
        Assertions.assertEquals(1500, result.listPrice)
        Assertions.assertEquals("001004008", result.category)
        Assertions.assertEquals("テスト本の説明", result.description)
        Assertions.assertEquals("https://example.com/small.jpg", result.smallThumbnailUrl)
        Assertions.assertEquals("https://example.com/medium.jpg", result.thumbnailUrl)
        Assertions.assertEquals("https://example.com/large.jpg", result.largeThumbnailUrl)
        Assertions.assertEquals("https://books.rakuten.co.jp/rb/12345/", result.rakutenItemUrl)
        Assertions.assertNull(result.rakutenAffiliateUrl)
        Assertions.assertTrue(result.isSearchedRakuten)
        Assertions.assertFalse(result.isSearchedGoogle)
        Assertions.assertFalse(result.isSearchedNDL)
        Assertions.assertEquals(0, result.registrationCount)
    }

    @Test
    @DisplayName("findByIsbn_正常系_countが0の場合nullを返す")
    fun findByIsbnCountZero() {
        val rt = mockRestTemplate(mapOf("count" to 0, "Items" to emptyList<Any>()))
        Assertions.assertNull(createClient(rt).findByIsbn("9784000000001"))
    }

    @Test
    @DisplayName("findByIsbn_正常系_APIが例外をスローした場合nullを返す")
    fun findByIsbnExceptionReturnsNull() {
        val rt = mockk<RestTemplate>()
        every {
            rt.exchange(any<String>(), HttpMethod.GET, any(), Map::class.java)
        } throws RuntimeException("Network error")

        Assertions.assertNull(createClient(rt).findByIsbn("9784000000001"))
    }

    @Test
    @DisplayName("findByIsbn_正常系_ISBN10桁の場合isbn10に設定されisbn13はnull")
    fun findByIsbnIsbn10Length() {
        val rt = mockRestTemplate(
            mapOf(
                "count" to 1,
                "Items" to listOf(
                    mapOf(
                        "title" to "テスト書籍",
                        "author" to "著者",
                        "publisherName" to "出版社",
                        "isbn" to "4000000001",
                        "salesDate" to "",
                        "itemPrice" to 0,
                        "booksGenreId" to "",
                        "itemCaption" to "",
                        "smallImageUrl" to "",
                        "mediumImageUrl" to "",
                        "largeImageUrl" to "",
                        "itemUrl" to "",
                        "affiliateUrl" to "",
                        "size" to 0,
                    ),
                ),
            ),
        )

        val result = createClient(rt).findByIsbn("4000000001")
        Assertions.assertNotNull(result)
        Assertions.assertEquals("4000000001", result!!.isbn10)
        Assertions.assertNull(result.isbn13)
    }

    @Test
    @DisplayName("findByIsbn_正常系_著者が空の場合emptyListになる")
    fun findByIsbnEmptyAuthor() {
        val rt = mockRestTemplate(
            mapOf(
                "count" to 1,
                "Items" to listOf(
                    mapOf(
                        "title" to "タイトル",
                        "author" to "",
                        "publisherName" to "出版社",
                        "isbn" to "9784000000001",
                        "salesDate" to "2024年01月",
                        "itemPrice" to 0,
                        "booksGenreId" to "",
                        "itemCaption" to "",
                        "smallImageUrl" to "",
                        "mediumImageUrl" to "",
                        "largeImageUrl" to "",
                        "itemUrl" to "",
                        "affiliateUrl" to "",
                        "size" to 0,
                    ),
                ),
            ),
        )

        val result = createClient(rt).findByIsbn("9784000000001")
        Assertions.assertNotNull(result)
        Assertions.assertEquals(emptyList<String>(), result!!.author)
    }

    @Test
    @DisplayName("findByIsbn_正常系_largeImageUrlが空の場合mediumImageUrlをlargeThumbnailUrlに使う")
    fun findByIsbnLargeImageUrlFallsBackToMedium() {
        val rt = mockRestTemplate(
            mapOf(
                "count" to 1,
                "Items" to listOf(
                    mapOf(
                        "title" to "タイトル",
                        "author" to "著者",
                        "publisherName" to "出版社",
                        "isbn" to "9784000000001",
                        "salesDate" to "2024年01月",
                        "itemPrice" to 0,
                        "booksGenreId" to "",
                        "itemCaption" to "",
                        "smallImageUrl" to "",
                        "mediumImageUrl" to "https://example.com/medium.jpg",
                        "largeImageUrl" to "",
                        "itemUrl" to "",
                        "affiliateUrl" to "",
                        "size" to 0,
                    ),
                ),
            ),
        )

        val result = createClient(rt).findByIsbn("9784000000001")
        Assertions.assertNotNull(result)
        Assertions.assertEquals("https://example.com/medium.jpg", result!!.largeThumbnailUrl)
    }

    @Test
    @DisplayName("findByIsbn_正常系_affiliateIdが空でないURLが付与される")
    fun findByIsbnWithAffiliateId() {
        val rt = mockRestTemplate(
            mapOf(
                "count" to 1,
                "Items" to listOf(
                    mapOf(
                        "title" to "タイトル",
                        "author" to "著者",
                        "publisherName" to "出版社",
                        "isbn" to "9784000000001",
                        "salesDate" to "2024年01月",
                        "itemPrice" to 0,
                        "booksGenreId" to "",
                        "itemCaption" to "",
                        "smallImageUrl" to "",
                        "mediumImageUrl" to "",
                        "largeImageUrl" to "",
                        "itemUrl" to "https://books.rakuten.co.jp/rb/12345/",
                        "affiliateUrl" to "https://af.rakuten.co.jp/abc",
                        "size" to 0,
                    ),
                ),
            ),
        )

        val client = RakutenBooksApiClient(
            applicationId = "test-app-id",
            accessKey = "test-access-key",
            affiliateId = "aff-id-123",
            siteUrl = "https://example.com",
            restTemplate = rt,
        )

        val result = client.findByIsbn("9784000000001")
        Assertions.assertNotNull(result)
        Assertions.assertEquals("https://af.rakuten.co.jp/abc", result!!.rakutenAffiliateUrl)
    }
}
