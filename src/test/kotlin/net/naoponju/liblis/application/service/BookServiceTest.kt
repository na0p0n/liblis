package net.naoponju.liblis.application.service

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import net.naoponju.liblis.domain.entity.BookEntity
import net.naoponju.liblis.domain.repository.BookRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

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

        coEvery {
            bookRepository.findBookByISBN(isbn = isbn)
        } returns defaultBookEntity

        val actual = bookService.findBookByISBN(isbn)
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

        coEvery {
            bookRepository.findBookByISBN(isbn = isbn)
        } returns null

        coEvery {
            bookRepository.findBookByISBNFromGoogle(isbn = isbn)
        } returns defaultBookEntity

        val actual = bookService.findBookByISBN(isbn)
        Assertions.assertEquals(expect, actual)
    }

    companion object {
        private const val DEFAULT_ISBN = "1111222233334"
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
