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

    fun isOwnedByUser(
        userId: UUID,
        userBooksId: UUID,
    ): Boolean {
        return userBooksRepository.existsByUserIdAndUserBooksId(
            userId,
            userBooksId,
        )
    }
}
