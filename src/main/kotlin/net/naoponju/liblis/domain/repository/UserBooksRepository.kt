package net.naoponju.liblis.domain.repository

import net.naoponju.liblis.application.dto.UserBooksDto
import java.util.UUID

interface UserBooksRepository {
    fun getUserBooksList(userId: UUID): List<UserBooksDto>?

    fun getRecentAddedBooks(): List<UUID>

    fun existsByUserIdAndBookId(
        userId: UUID,
        bookId: UUID,
    ): Boolean

    fun fetchUserBooksIdFromUserIdAndBookId(
        userId: UUID,
        bookId: UUID,
    ): UUID?

    fun existsByUserIdAndUserBooksId(
        userId: UUID,
        userBooksId: UUID,
    ): Boolean

    fun insertUserBooksData(userBooksDto: UserBooksDto): UUID?

    fun editUserBooksData(userBooksDto: UserBooksDto)

    /**
 * Deletes the user-books record identified by the given `userBooksId`.
 *
 * @param userBooksId The UUID of the user-books record to delete.
 */
fun deleteUserBooks(userBooksId: UUID)

    /**
 * Counts how many user-books records are associated with the specified user.
 *
 * @param userId The UUID of the user whose user-books records will be counted.
 * @return The number of user-books records for the given user.
 */
fun countUserBooks(userId: UUID): Int

    /**
     * Retrieve the user's book record identified by the given user ID and book ID (for book detail pages).
     *
     * @param userId The UUID of the user.
     * @param bookId The UUID of the book.
     * @return The matching `UserBooksDto` if found, `null` otherwise.
     */
    fun findUserBookByBookId(
        userId: UUID,
        bookId: UUID,
    ): UserBooksDto?
}
