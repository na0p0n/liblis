package net.naoponju.liblis.application.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import net.naoponju.liblis.common.exception.BookNotFoundException
import net.naoponju.liblis.domain.entity.BookEntity
import net.naoponju.liblis.domain.repository.BookRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    @DisplayName("ISBN書籍検索_正常系_楽天ブックスAPIから検索")
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
            bookRepository.findBookByISBNFromRakuten(isbn = isbn)
        } returns defaultBookEntity

        val actual = bookService.findBookByISBN(isbn)
        verify(exactly = 1) { bookRepository.findBookByISBNFromRakuten(isbn = isbn) }
        verify(exactly = 0) { bookRepository.findBookByISBNFromGoogle(isbn = any()) }
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
        } returns emptyList()

        val actual = bookService.fetchUserHavingBookIdsInBookIdList(userId, bookIds)
        Assertions.assertEquals(emptyList<BookEntity>(), actual)
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

    @Test
    @DisplayName("BookIDリストから書籍取得_正常系_書籍あり")
    fun findBookListByBookIdsSuccess01() {
        val bookIds = listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        val expect = listOf(defaultBookEntity)

        every { bookRepository.findBookListByBookIdList(bookIds) } returns listOf(defaultBookEntity)

        val actual = bookService.findBookListByBookIds(bookIds)
        Assertions.assertEquals(expect, actual)
        verify(exactly = 1) { bookRepository.findBookListByBookIdList(bookIds) }
    }

    @Test
    @DisplayName("BookIDリストから書籍取得_正常系_空リストを渡すとリポジトリを呼ばず空リストを返す")
    fun findBookListByBookIdsSuccess02() {
        val actual = bookService.findBookListByBookIds(emptyList())
        Assertions.assertEquals(emptyList<BookEntity>(), actual)
        verify(exactly = 0) { bookRepository.findBookListByBookIdList(any()) }
    }

    @Test
    @DisplayName("BookIDリストから書籍取得_正常系_リポジトリが空リストを返す")
    fun findBookListByBookIdsSuccess03() {
        val bookIds = listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))

        every { bookRepository.findBookListByBookIdList(bookIds) } returns emptyList()

        val actual = bookService.findBookListByBookIds(bookIds)
        Assertions.assertEquals(emptyList<BookEntity>(), actual)
    }

    @Test
    @DisplayName("ユーザー所持書籍ページング取得_正常系_nullを返すリポジトリはnullを返す")
    fun getHavingBooksPagedNullFromRepo() {
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
    @DisplayName("全書籍件数取得_正常系_大きな件数")
    fun getAllBookCountLargeCount() {
        val expect = 999_999

        every { bookRepository.countAllBooks() } returns 999_999

        val actual = bookService.getAllBookCount()
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("書籍一覧ページング取得_正常系_ページ2の場合オフセットが正しい")
    fun getBookListPagedPage2() {
        val offset = 20
        val limit = 20
        val expect = listOf(defaultBookEntity)

        every { bookRepository.findAllPaged(offset, limit) } returns listOf(defaultBookEntity)

        val actual = bookService.getBookListPaged(offset, limit)
        Assertions.assertEquals(expect, actual)
        verify(exactly = 1) { bookRepository.findAllPaged(offset, limit) }
    }

    @Test
    @DisplayName("ユーザー所持書籍IDリスト取得_正常系_書籍IDが存在するがリポジトリがnullを返す場合はnullを返す")
    fun fetchUserHavingBookIdsInBookIdListReturnsNullWhenRepoReturnsNull() {
        val userId = DEFAULT_USER_ID
        val bookIds = listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))

        every {
            bookRepository.fetchUserHavingBookIdsInBookIdList(userId, bookIds)
        } returns null

        val actual = bookService.fetchUserHavingBookIdsInBookIdList(userId, bookIds)
        Assertions.assertNull(actual)
    }

    @Test
    @DisplayName("書籍詳細取得_正常系_書籍が見つかる")
    fun findBookByIdSuccess01() {
        val bookId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val expect = defaultBookEntity.copy(id = bookId)

        every { bookRepository.findById(bookId) } returns expect

        val actual = bookService.findBookById(bookId)
        Assertions.assertEquals(expect, actual)
        verify(exactly = 1) { bookRepository.findById(bookId) }
    }

    @Test
    @DisplayName("書籍詳細取得_正常系_書籍が見つからない場合nullを返す")
    fun findBookByIdSuccess02() {
        val bookId = UUID.fromString("00000000-0000-0000-0000-000000000001")

        every { bookRepository.findById(bookId) } returns null

        val actual = bookService.findBookById(bookId)
        Assertions.assertNull(actual)
        verify(exactly = 1) { bookRepository.findById(bookId) }
    }

    @Test
    @DisplayName("書籍詳細取得_境界値_異なるIDでは別の書籍を返す")
    fun findBookByIdDifferentIdsReturnDifferentBooks() {
        val bookId1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val bookId2 = UUID.fromString("00000000-0000-0000-0000-000000000002")
        val book1 = defaultBookEntity.copy(id = bookId1, title = "Book 1")
        val book2 = defaultBookEntity.copy(id = bookId2, title = "Book 2")

        every { bookRepository.findById(bookId1) } returns book1
        every { bookRepository.findById(bookId2) } returns book2

        Assertions.assertEquals(book1, bookService.findBookById(bookId1))
        Assertions.assertEquals(book2, bookService.findBookById(bookId2))
    }

    @Test
    @DisplayName("ISBN書籍検索_異常系_楽天ブックスAPIがnullを返す場合はBookNotFoundExceptionをスロー")
    fun findBookByISBNThrowsWhenRakutenReturnsNull() {
        val isbn = DEFAULT_ISBN

        every { bookRepository.findBookByISBN(isbn = isbn) } returns null
        every { bookRepository.findBookByISBNFromRakuten(isbn = isbn) } returns null

        assertThrows<BookNotFoundException> {
            bookService.findBookByISBN(isbn)
        }
        verify(exactly = 0) { bookRepository.insert(any<BookEntity>()) }
    }

    @Test
    @DisplayName("タイトル書籍検索_正常系_書籍あり")
    fun findByTitleSuccess01() {
        val title = "テスト本"
        val expect = listOf(defaultBookEntity.copy(title = title))

        every { bookRepository.findBookByTitle(title) } returns listOf(defaultBookEntity.copy(title = title))

        val actual = bookService.findByTitle(title)
        Assertions.assertEquals(expect, actual)
        verify(exactly = 1) { bookRepository.findBookByTitle(title) }
    }

    @Test
    @DisplayName("タイトル書籍検索_正常系_検索結果なし")
    fun findByTitleSuccess02() {
        val title = "存在しない本"

        every { bookRepository.findBookByTitle(title) } returns emptyList()

        val actual = bookService.findByTitle(title)
        Assertions.assertEquals(emptyList<BookEntity>(), actual)
    }

    @Test
    @DisplayName("タイトル書籍検索_正常系_複数件を返す")
    fun findByTitleReturnsMultiple() {
        val title = "プログラミング"
        val books = listOf(
            defaultBookEntity.copy(title = "プログラミングKotlin"),
            defaultBookEntity.copy(title = "プログラミングJava"),
        )

        every { bookRepository.findBookByTitle(title) } returns books

        val actual = bookService.findByTitle(title)
        Assertions.assertEquals(2, actual.size)
        Assertions.assertEquals(books, actual)
    }

    @Test
    @DisplayName("著者書籍検索_正常系_書籍あり")
    fun findByAuthorSuccess01() {
        val author = "著者テスト"
        val expect = listOf(defaultBookEntity.copy(author = listOf(author)))

        every { bookRepository.findBookByAuthor(author) } returns listOf(defaultBookEntity.copy(author = listOf(author)))

        val actual = bookService.findByAuthor(author)
        Assertions.assertEquals(expect, actual)
        verify(exactly = 1) { bookRepository.findBookByAuthor(author) }
    }

    @Test
    @DisplayName("著者書籍検索_正常系_検索結果なし")
    fun findByAuthorSuccess02() {
        val author = "存在しない著者"

        every { bookRepository.findBookByAuthor(author) } returns emptyList()

        val actual = bookService.findByAuthor(author)
        Assertions.assertEquals(emptyList<BookEntity>(), actual)
    }

    @Test
    @DisplayName("著者書籍検索_正常系_複数件を返す")
    fun findByAuthorReturnsMultiple() {
        val author = "テスト著者"
        val books = listOf(
            defaultBookEntity.copy(author = listOf(author, "共著者A")),
            defaultBookEntity.copy(author = listOf(author, "共著者B")),
        )

        every { bookRepository.findBookByAuthor(author) } returns books

        val actual = bookService.findByAuthor(author)
        Assertions.assertEquals(2, actual.size)
    }

    @Test
    @DisplayName("ユーザー書庫タイトル検索_正常系_書籍あり")
    fun findUserBooksByTitleSuccess01() {
        val userId = DEFAULT_USER_ID
        val title = "マイ本"
        val expect = listOf(defaultBookEntity.copy(title = title))

        every { bookRepository.findUserBooksByTitle(userId, title) } returns listOf(defaultBookEntity.copy(title = title))

        val actual = bookService.findUserBooksByTitle(userId, title)
        Assertions.assertEquals(expect, actual)
        verify(exactly = 1) { bookRepository.findUserBooksByTitle(userId, title) }
    }

    @Test
    @DisplayName("ユーザー書庫タイトル検索_正常系_検索結果なし")
    fun findUserBooksByTitleSuccess02() {
        val userId = DEFAULT_USER_ID
        val title = "存在しない本"

        every { bookRepository.findUserBooksByTitle(userId, title) } returns emptyList()

        val actual = bookService.findUserBooksByTitle(userId, title)
        Assertions.assertEquals(emptyList<BookEntity>(), actual)
    }

    @Test
    @DisplayName("ユーザー書庫タイトル検索_正常系_正確なuserIdとtitleが渡される")
    fun findUserBooksByTitlePassesCorrectArgs() {
        val userId = DEFAULT_USER_ID
        val title = "テスト"
        val otherId = UUID.fromString("00000000-0000-0000-0000-000000000001")

        every { bookRepository.findUserBooksByTitle(userId, title) } returns emptyList()

        bookService.findUserBooksByTitle(userId, title)

        verify(exactly = 1) { bookRepository.findUserBooksByTitle(userId, title) }
        verify(exactly = 0) { bookRepository.findUserBooksByTitle(otherId, title) }
    }

    @Test
    @DisplayName("ユーザー書庫著者検索_正常系_書籍あり")
    fun findUserBooksByAuthorSuccess01() {
        val userId = DEFAULT_USER_ID
        val author = "マイ著者"
        val expect = listOf(defaultBookEntity.copy(author = listOf(author)))

        every { bookRepository.findUserBooksByAuthor(userId, author) } returns listOf(defaultBookEntity.copy(author = listOf(author)))

        val actual = bookService.findUserBooksByAuthor(userId, author)
        Assertions.assertEquals(expect, actual)
        verify(exactly = 1) { bookRepository.findUserBooksByAuthor(userId, author) }
    }

    @Test
    @DisplayName("ユーザー書庫著者検索_正常系_検索結果なし")
    fun findUserBooksByAuthorSuccess02() {
        val userId = DEFAULT_USER_ID
        val author = "存在しない著者"

        every { bookRepository.findUserBooksByAuthor(userId, author) } returns emptyList()

        val actual = bookService.findUserBooksByAuthor(userId, author)
        Assertions.assertEquals(emptyList<BookEntity>(), actual)
    }

    @Test
    @DisplayName("ユーザー書庫著者検索_正常系_正確なuserIdとauthorが渡される")
    fun findUserBooksByAuthorPassesCorrectArgs() {
        val userId = DEFAULT_USER_ID
        val author = "テスト著者"
        val otherId = UUID.fromString("00000000-0000-0000-0000-000000000001")

        every { bookRepository.findUserBooksByAuthor(userId, author) } returns emptyList()

        bookService.findUserBooksByAuthor(userId, author)

        verify(exactly = 1) { bookRepository.findUserBooksByAuthor(userId, author) }
        verify(exactly = 0) { bookRepository.findUserBooksByAuthor(otherId, author) }
    }

    @Test
    @DisplayName("ISBN書籍検索_異常系_BookNotFoundExceptionにISBNが含まれるメッセージ")
    fun findBookByISBNExceptionContainsIsbn() {
        val isbn = "9784000000001"

        every { bookRepository.findBookByISBN(isbn = isbn) } returns null
        every { bookRepository.findBookByISBNFromRakuten(isbn = isbn) } returns null

        val exception = assertThrows<BookNotFoundException> {
            bookService.findBookByISBN(isbn)
        }
        Assertions.assertTrue(exception.message!!.contains(isbn))
    }

    companion object {
        private const val DEFAULT_ISBN = "1111222233334"
        private val DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000099")
        private val defaultBookEntity =
            BookEntity(
                id = null,
                title = null,
                titleKana = null,
                subTitle = null,
                subTitleKana = null,
                author = emptyList(),
                publisher = null,
                bookSize = null,
                publishDate = null,
                pages = null,
                description = null,
                isbn10 = null,
                isbn13 = "1111222233334",
                listPrice = null,
                category = null,
                smallThumbnailUrl = null,
                thumbnailUrl = null,
                largeThumbnailUrl = null,
                registrationCount = 0,
                isSearchedNDL = false,
                ndlUrl = null,
                isSearchedGoogle = false,
                googleUrl = null,
                isSearchedRakuten = false,
                rakutenItemUrl = null,
                rakutenAffiliateUrl = null,
                createdAt = null,
                updatedAt = null,
            )
    }
}