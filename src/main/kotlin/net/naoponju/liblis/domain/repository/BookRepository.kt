package net.naoponju.liblis.domain.repository

import net.naoponju.liblis.domain.entity.BookEntity
import java.util.UUID

interface BookRepository {
    fun findBookByISBN(isbn: String): BookEntity?

    fun findBookByISBNFromGoogle(isbn: String): BookEntity

    fun fetchAllBooks(): List<BookEntity>

    fun fetchUserHavingBooks(userId: UUID): List<BookEntity>

    fun findBookByTitle(title: String): List<BookEntity>

    fun findBookByAuthor(author: String): List<BookEntity>

    fun fetchRecentBooks(limit: Int): List<BookEntity>

    fun findAllPaged(
        offset: Int,
        limit: Int,
    ): List<BookEntity>

    fun countAllBooks(): Int

    fun insert(book: BookEntity)
}
