package net.naoponju.liblis.infra.repository.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import net.naoponju.liblis.application.dto.GoogleBookDataDto
import net.naoponju.liblis.common.exception.BookNotFoundException
import net.naoponju.liblis.domain.entity.BookEntity
import net.naoponju.liblis.infra.api.GoogleBooksApiClient
import net.naoponju.liblis.infra.mapper.BookMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class BookRepositoryImplTest {
    @AfterEach
    fun tearDown() {
        unmockkStatic(UUID::class)
        unmockkStatic(LocalDateTime::class)
    }

    private val bookMapper: BookMapper = mockk()
    private val googleBooksApiClient: GoogleBooksApiClient = mockk()

    private val bookRepositoryImpl =
        spyk(
            objToCopy =
                BookRepositoryImpl(
                    bookMapper = bookMapper,
                    googleBooksApiClient = googleBooksApiClient,
                ),
        )

    @Test
    @DisplayName("ISBN書籍検索_正常系")
    fun findBookByISBNSuccess01() {
        val isbn = DEFAULT_ISBN
        val expect = defaultBookEntity

        every {
            bookMapper.findByISBN(isbn = isbn)
        } returns defaultBookEntity

        val actual = bookRepositoryImpl.findBookByISBN(isbn = isbn)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("ISBN書籍検索_正常系_書籍が見つからない")
    fun findBookByISBNSuccess02() {
        val isbn = DEFAULT_ISBN
        val expect = null

        every {
            bookMapper.findByISBN(isbn = isbn)
        } returns null

        val actual = bookRepositoryImpl.findBookByISBN(isbn = isbn)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("全書籍取得_正常系")
    fun fetchAllBooksSuccess01() {
        val expect =
            listOf(
                defaultBookEntity,
            )

        every {
            bookMapper.fetchAllBooksOrderByTitle()
        } returns
            listOf(
                defaultBookEntity,
            )

        val actual = bookRepositoryImpl.fetchAllBooks()
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("全書籍取得_正常系_書籍がない")
    fun fetchAllBooksSuccess02() {
        val expect = emptyList<BookEntity>()

        every {
            bookMapper.fetchAllBooksOrderByTitle()
        } returns emptyList()

        val actual = bookRepositoryImpl.fetchAllBooks()
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("タイトル書籍検索_正常系")
    fun findBookByTitleSuccess01() {
        val title = "sample"
        val expect =
            listOf(
                defaultBookEntity.copy(
                    title = "sample",
                ),
            )

        every {
            bookMapper.findBookByTitle(title = title)
        } returns
            listOf(
                defaultBookEntity.copy(
                    title = "sample",
                ),
            )

        val actual = bookRepositoryImpl.findBookByTitle(title)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("タイトル書籍検索_正常系_検索結果なし")
    fun findBookByTitleSuccess02() {
        val title = "sample"
        val expect = emptyList<BookEntity>()

        every {
            bookMapper.findBookByTitle(title = title)
        } returns emptyList()

        val actual = bookRepositoryImpl.findBookByTitle(title)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("著者から書籍検索_正常系")
    fun findBookByAuthorSuccess01() {
        val author = "sample"
        val expect =
            listOf(
                defaultBookEntity.copy(
                    author =
                        listOf(
                            author,
                        ),
                ),
            )

        every {
            bookMapper.findBookByAuthor(author = author)
        } returns
            listOf(
                defaultBookEntity.copy(
                    author =
                        listOf(
                            author,
                        ),
                ),
            )

        val actual = bookRepositoryImpl.findBookByAuthor(author)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("著者から書籍検索_正常系_検索結果なし")
    fun findBookByAuthorSuccess02() {
        val author = "sample"
        val expect = emptyList<BookEntity>()

        every {
            bookMapper.findBookByAuthor(author = author)
        } returns emptyList()

        val actual = bookRepositoryImpl.findBookByAuthor(author)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("直近に追加された書籍検索_正常系")
    fun fetchRecentBooksSuccess01() {
        val limit = 10
        val expect =
            listOf(
                defaultBookEntity,
                defaultBookEntity,
            )

        every {
            bookMapper.fetchRecentBooks(limit = limit)
        } returns
            listOf(
                defaultBookEntity,
                defaultBookEntity,
            )

        val actual = bookRepositoryImpl.fetchRecentBooks(limit)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("直近に追加された書籍検索_正常系_検索結果なし")
    fun fetchRecentBooksSuccess02() {
        val limit = 10
        val expect = emptyList<BookEntity>()

        every {
            bookMapper.fetchRecentBooks(limit = limit)
        } returns emptyList()

        val actual = bookRepositoryImpl.fetchRecentBooks(limit)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("Googleから書籍データ検索_正常系")
    fun findBookByISBNFromGoogleSuccess() {
        val fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000000")

        // UUIDクラスをstatic mock化する
        mockkStatic(UUID::class)
        every { UUID.randomUUID() } returns fixedUuid

        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns null

        val expect =
            defaultBookEntity.copy(
                id = fixedUuid,
                isSearchedGoogle = true,
            )

        every {
            googleBooksApiClient.fetchBookData(DEFAULT_ISBN)
        } returns
            GoogleBookDataDto(
                title = null,
                authors = emptyList(),
                publisher = null,
                publishedDate = null,
                description = null,
                isbn10 = null,
                isbn13 = DEFAULT_ISBN,
                pageCount = null,
                bookThumbnailUrl = null,
                selfLink = null,
            )

        val actual = bookRepositoryImpl.findBookByISBNFromGoogle(DEFAULT_ISBN)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("Googleから書籍データ検索_異常系_該当する検索結果がない")
    fun findBookByISBNFromGoogleFailure01() {
        val errorMessage = "対象のISBNの本がGoogleBooksで見つかりません"

        every {
            googleBooksApiClient.fetchBookData(DEFAULT_ISBN)
        } throws BookNotFoundException("対象のISBNの本がGoogleBooksで見つかりません")

        val actual =
            Assertions.assertThrows(BookNotFoundException::class.java) {
                bookRepositoryImpl.findBookByISBNFromGoogle(DEFAULT_ISBN)
            }

        Assertions.assertEquals(errorMessage, actual.message)
    }

    @Test
    @DisplayName("Googleから書籍データ検索_異常系_selfLinkが取得できない")
    fun findBookByISBNFromGoogleFailure02() {
        val errorMessage = "selfLink が取得できませんでした。 ISBN: $DEFAULT_ISBN"

        every {
            googleBooksApiClient.fetchBookData(DEFAULT_ISBN)
        } throws BookNotFoundException("selfLink が取得できませんでした。 ISBN: $DEFAULT_ISBN")

        val actual =
            Assertions.assertThrows(BookNotFoundException::class.java) {
                bookRepositoryImpl.findBookByISBNFromGoogle(DEFAULT_ISBN)
            }

        Assertions.assertEquals(errorMessage, actual.message)
    }

    @Test
    @DisplayName("ユーザー所持書籍IDリスト検索_正常系")
    fun fetchUserHavingBookIdsInBookIdListSuccess01() {
        val userId = DEFAULT_USER_ID
        val bookIds = listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        val expect = listOf(defaultBookEntity)

        every {
            bookMapper.fetchUserHavingBookIdsInBookIdList(userId, bookIds)
        } returns listOf(defaultBookEntity)

        val actual = bookRepositoryImpl.fetchUserHavingBookIdsInBookIdList(userId, bookIds)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("ユーザー所持書籍IDリスト検索_正常系_nullを返す")
    fun fetchUserHavingBookIdsInBookIdListSuccess02() {
        val userId = DEFAULT_USER_ID
        val bookIds = emptyList<UUID>()

        every {
            bookMapper.fetchUserHavingBookIdsInBookIdList(userId, bookIds)
        } returns null

        val actual = bookRepositoryImpl.fetchUserHavingBookIdsInBookIdList(userId, bookIds)
        Assertions.assertNull(actual)
    }

    @Test
    @DisplayName("ユーザー所持書籍ページング取得_正常系")
    fun fetchUserHavingBooksPagedSuccess01() {
        val userId = DEFAULT_USER_ID
        val offset = 0
        val limit = 20
        val expect = listOf(defaultBookEntity)

        every {
            bookMapper.fetchUserBooksPaged(userId, offset, limit)
        } returns listOf(defaultBookEntity)

        val actual = bookRepositoryImpl.fetchUserHavingBooksPaged(userId, offset, limit)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("ユーザー所持書籍ページング取得_正常系_mapperがnullを返す場合は空リスト")
    fun fetchUserHavingBooksPagedSuccess02() {
        val userId = DEFAULT_USER_ID
        val offset = 0
        val limit = 20

        every {
            bookMapper.fetchUserBooksPaged(userId, offset, limit)
        } returns null

        val actual = bookRepositoryImpl.fetchUserHavingBooksPaged(userId, offset, limit)
        Assertions.assertEquals(emptyList<BookEntity>(), actual)
    }

    @Test
    @DisplayName("書籍一覧ページング取得_正常系")
    fun findAllPagedSuccess01() {
        val offset = 0
        val limit = 20
        val expect = listOf(defaultBookEntity)

        every { bookMapper.findAllPaged(offset, limit) } returns listOf(defaultBookEntity)

        val actual = bookRepositoryImpl.findAllPaged(offset, limit)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("書籍一覧ページング取得_正常系_空リスト")
    fun findAllPagedSuccess02() {
        val offset = 20
        val limit = 20

        every { bookMapper.findAllPaged(offset, limit) } returns emptyList()

        val actual = bookRepositoryImpl.findAllPaged(offset, limit)
        Assertions.assertEquals(emptyList<BookEntity>(), actual)
    }

    @Test
    @DisplayName("全書籍件数取得_正常系")
    fun countAllBooksSuccess01() {
        every { bookMapper.countAllBooks() } returns 100

        val actual = bookRepositoryImpl.countAllBooks()
        Assertions.assertEquals(100, actual)
    }

    @Test
    @DisplayName("全書籍件数取得_正常系_0件")
    fun countAllBooksSuccess02() {
        every { bookMapper.countAllBooks() } returns 0

        val actual = bookRepositoryImpl.countAllBooks()
        Assertions.assertEquals(0, actual)
    }

    companion object {
        private const val DEFAULT_ISBN = "1111222233334"
        private val DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000099")
        private val defaultBookEntity =
            BookEntity(
                id = null,
                title = null,
                author = emptyList(),
                publisher = null,
                publishDate = null,
                pages = null,
                description = null,
                isbn10 = null,
                isbn13 = "1111222233334",
                listPrice = null,
                category = null,
                thumbnailUrl = null,
                registrationCount = 0,
                isSearchedNDL = false,
                ndlUrl = null,
                isSearchedGoogle = false,
                googleUrl = null,
                createdAt = null,
                updatedAt = null,
            )
    }
}