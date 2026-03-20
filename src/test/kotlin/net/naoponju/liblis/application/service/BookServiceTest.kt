package net.naoponju.liblis.application.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import net.naoponju.liblis.domain.entity.BookEntity
import net.naoponju.liblis.domain.repository.BookRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

class BookServiceTest {
    private val bookRepository: BookRepository = mockk(relaxed = true)

    private val bookService =
        spyk(
            objToCopy =
                BookService(
                    bookRepository = bookRepository,
                ),
        )

    @Test
    @DisplayName("ISBN書籍検索_正常系_DBから検索")
    fun findBookByISBNSuccess01() {
        val isbn = DEFAULT_ISBN
        val expect: Pair<BookEntity, Boolean> =
            Pair(
                defaultBookEntity,
                true,
            )

        every {
            bookRepository.findBookByISBN(isbn = isbn)
        } returns defaultBookEntity

        val actual = bookService.findBookByISBN(isbn)
        verify(exactly = 0) { bookRepository.insert(any<BookEntity>()) }
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("ISBN書籍検索_正常系_GoogleBooksAPIから検索")
    fun findBookByISBNSuccess02() {
        val isbn = DEFAULT_ISBN
        val expect: Pair<BookEntity, Boolean> =
            Pair(
                defaultBookEntity,
                false,
            )

        every {
            bookRepository.findBookByISBN(isbn = isbn)
        } returns null

        every {
            bookRepository.findBookByISBNFromGoogle(isbn = isbn)
        } returns defaultBookEntity

        val actual = bookService.findBookByISBN(isbn)
        verify(exactly = 1) { bookRepository.insert(any<BookEntity>()) }
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("全書籍件数取得_正常系")
    fun getAllBookCountSuccess01() {
        val expect = 42

        every { bookRepository.countAllBooks() } returns 42

        val actual = bookService.getAllBookCount()
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("全書籍件数取得_正常系_0件")
    fun getAllBookCountSuccess02() {
        every { bookRepository.countAllBooks() } returns 0

        val actual = bookService.getAllBookCount()
        Assertions.assertEquals(0, actual)
    }

    @Test
    @DisplayName("ユーザー所持書籍IDリスト取得_正常系_書籍あり")
    fun fetchUserHavingBookIdsInBookIdListSuccess01() {
        val userId = DEFAULT_USER_ID
        val bookIds = listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        val expect = listOf(defaultBookEntity)

        every {
            bookRepository.fetchUserHavingBookIdsInBookIdList(userId, bookIds)
        } returns listOf(defaultBookEntity)

        val actual = bookService.fetchUserHavingBookIdsInBookIdList(userId, bookIds)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("ユーザー所持書籍IDリスト取得_正常系_nullを返す")
    fun fetchUserHavingBookIdsInBookIdListSuccess02() {
        val userId = DEFAULT_USER_ID
        val bookIds = emptyList<UUID>()

        every {
            bookRepository.fetchUserHavingBookIdsInBookIdList(userId, bookIds)
        } returns null

        val actual = bookService.fetchUserHavingBookIdsInBookIdList(userId, bookIds)
        Assertions.assertNull(actual)
    }

    @Test
    @DisplayName("ユーザー所持書籍ページング取得_正常系")
    fun getHavingBooksPagedSuccess01() {
        val userId = DEFAULT_USER_ID
        val offset = 0
        val limit = 20
        val expect = listOf(defaultBookEntity)

        every {
            bookRepository.fetchUserHavingBooksPaged(userId, offset, limit)
        } returns listOf(defaultBookEntity)

        val actual = bookService.getHavingBooksPaged(userId, offset, limit)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("ユーザー所持書籍ページング取得_正常系_空リスト")
    fun getHavingBooksPagedSuccess02() {
        val userId = DEFAULT_USER_ID
        val offset = 0
        val limit = 20

        every {
            bookRepository.fetchUserHavingBooksPaged(userId, offset, limit)
        } returns emptyList()

        val actual = bookService.getHavingBooksPaged(userId, offset, limit)
        Assertions.assertEquals(emptyList<BookEntity>(), actual)
    }

    @Test
    @DisplayName("書籍一覧ページング取得_正常系")
    fun getBookListPagedSuccess01() {
        val offset = 0
        val limit = 20
        val expect = listOf(defaultBookEntity)

        every { bookRepository.findAllPaged(offset, limit) } returns listOf(defaultBookEntity)

        val actual = bookService.getBookListPaged(offset, limit)
        Assertions.assertEquals(expect, actual)
        verify(exactly = 1) { bookRepository.findAllPaged(offset, limit) }
    }

    @Test
    @DisplayName("書籍一覧ページング取得_正常系_空リスト")
    fun getBookListPagedSuccess02() {
        val offset = 40
        val limit = 20

        every { bookRepository.findAllPaged(offset, limit) } returns emptyList()

        val actual = bookService.getBookListPaged(offset, limit)
        Assertions.assertEquals(emptyList<BookEntity>(), actual)
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
