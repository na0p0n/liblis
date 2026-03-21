package net.naoponju.liblis.domain.repository

import net.naoponju.liblis.domain.entity.BookEntity
import java.util.UUID

@Suppress("TooManyFunctions")
interface BookRepository {
    fun findBookByISBN(isbn: String): BookEntity?

    fun findBookListByBookIdList(bookIds: List<UUID>): List<BookEntity>

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

    fun findBookByTitle(title: String): List<BookEntity>

    fun findBookByAuthor(author: String): List<BookEntity>

    fun fetchRecentBooks(limit: Int): List<BookEntity>

    /**
     * Retrieve a page of books using offset/limit pagination.
     *
     * @param offset Number of records to skip before collecting the page.
     * @param limit Maximum number of records to return.
     * @return A list of BookEntity objects for the requested page; may be empty.
     */
    fun findAllPaged(
        offset: Int,
        limit: Int,
    ): List<BookEntity>

    /**
 * Retrieve a book by its primary identifier.
 *
 * @param id The book's UUID primary key.
 * @return `BookEntity` if a book with the given id exists, `null` otherwise.
 */
fun findById(id: UUID): BookEntity?

    /**
 * Get the total number of books in the repository.
 *
 * @return The total number of books in the repository.
 */
fun countAllBooks(): Int

    /**
 * Inserts the given book entity into the repository.
 *
 * @param book The BookEntity to persist.
 */
fun insert(book: BookEntity)
}
