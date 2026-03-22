package net.naoponju.liblis.domain.repository

import net.naoponju.liblis.domain.entity.BookEntity
import java.util.UUID

@Suppress("TooManyFunctions")
interface BookRepository {
    fun findBookByISBN(isbn: String): BookEntity?

    fun findBookListByBookIdList(bookIds: List<UUID>): List<BookEntity>

    fun findBookByISBNFromRakuten(isbn: String): BookEntity?

    fun findBookByISBNFromGoogle(isbn: String): BookEntity

    fun fetchAllBooks(): List<BookEntity>

    fun fetchUserHavingBookIdsInBookIdList(
        userId: UUID,
        bookIds: List<UUID>,
    ): List<BookEntity>?

    fun fetchUserHavingBooksPaged(
        userId: UUID,
        offset: Int,
        limit: Int,
    ): List<BookEntity>

    fun findUserBooksByTitle(
        userId: UUID,
        title: String,
    ): List<BookEntity>

    fun findUserBooksByAuthor(
        userId: UUID,
        author: String,
    ): List<BookEntity>

    fun findBookByTitle(title: String): List<BookEntity>

    fun findBookByAuthor(author: String): List<BookEntity>

    fun fetchRecentBooks(limit: Int): List<BookEntity>

    fun findAllPaged(
        offset: Int,
        limit: Int,
    ): List<BookEntity>

    fun findById(id: UUID): BookEntity?

    fun countAllBooks(): Int

    fun insert(book: BookEntity)
}
