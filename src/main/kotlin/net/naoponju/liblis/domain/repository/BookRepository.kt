package net.naoponju.liblis.domain.repository

import net.naoponju.liblis.application.dto.FoundBookDataDto
import net.naoponju.liblis.domain.entity.BookEntity

interface BookRepository {
    fun findBookByISBN(isbn: String): BookEntity?
    fun findBookByISBNFromGoogle(isbn: String): FoundBookDataDto?
    fun fetchAllBooks(): List<BookEntity>?
    fun findBookByTitle(title: String): List<BookEntity>?
    fun findBookByAuthor(author: String): List<BookEntity>?
    fun fetchRecentBooks(limit: Int): List<BookEntity>?
}
