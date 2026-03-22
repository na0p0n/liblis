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

    fun deleteUserBooks(userBooksId: UUID)

    fun countUserBooks(userId: UUID): Int

    // B-9: 書籍詳細ページ用
    fun findUserBookByBookId(
        userId: UUID,
        bookId: UUID,
    ): UserBooksDto?
}
