package net.naoponju.liblis.domain.repository

import net.naoponju.liblis.application.dto.UserBooksDto
import java.util.UUID

interface UserBooksRepository {
    fun getUserBooksList(userId: UUID): List<UserBooksDto>?

    fun insertUserBooksData(userBooksDto: UserBooksDto): UUID?

    fun editUserBooksData(userBooksDto: UserBooksDto)

    fun deleteUserBooks(userBooksId: UUID)
}
