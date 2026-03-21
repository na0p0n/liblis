package net.naoponju.liblis.application.service

import net.naoponju.liblis.application.dto.UserBooksDto
import net.naoponju.liblis.domain.repository.UserBooksRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserBooksService(
    private val userBooksRepository: UserBooksRepository,
) {
    fun getUserHavingBooks(userId: UUID): List<UserBooksDto>? {
        val result = userBooksRepository.getUserBooksList(userId)
        return result
    }

    fun getRecentAddedBooks(): List<UUID> {
        return userBooksRepository.getRecentAddedBooks()
    }

    fun insertUserBooksData(userBooksDto: UserBooksDto): UUID? {
        val resultUUID = userBooksRepository.insertUserBooksData(userBooksDto)
        return resultUUID
    }

    fun updateUserBooksData(userBooksDto: UserBooksDto) {
        userBooksRepository.editUserBooksData(userBooksDto)
    }

    fun deleteUserBooksData(userBooksId: UUID) {
        userBooksRepository.deleteUserBooks(userBooksId)
    }

    fun countUserBooks(userId: UUID): Int {
        return userBooksRepository.countUserBooks(userId)
    }

    /**
     * Retrieve the UserBooks record ID for a given user and book.
     *
     * @param userId The UUID of the user.
     * @param bookId The UUID of the book.
     * @return The UUID of the matching UserBooks record, or `null` if no match is found.
     */
    fun getUserBooksIdFromUserIdAndBookId(
        userId: UUID,
        bookId: UUID,
    ): UUID? {
        return userBooksRepository.fetchUserBooksIdFromUserIdAndBookId(userId, bookId)
    }

    /**
     * Checks whether the specified user is the owner of the given user-books record.
     *
     * @param userId The UUID of the user to check ownership for.
     * @param userBooksId The UUID of the user-books record to verify ownership of.
     * @return `true` if a user-books record with `userBooksId` exists and is associated with `userId`, `false` otherwise.
     */
    fun isOwnedByUser(
        userId: UUID,
        userBooksId: UUID,
    ): Boolean {
        return userBooksRepository.existsByUserIdAndUserBooksId(userId, userBooksId)
    }

    /**
     * Retrieves the user's ownership record for a specific book.
     *
     * @param userId The UUID of the user.
     * @param bookId The UUID of the book.
     * @return The matching `UserBooksDto` if found, `null` otherwise.
     */
    fun findUserBookByBookId(
        userId: UUID,
        bookId: UUID,
    ): UserBooksDto? {
        return userBooksRepository.findUserBookByBookId(userId, bookId)
    }
}
