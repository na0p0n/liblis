package net.naoponju.liblis.service

import net.naoponju.liblis.dto.UserDto
import net.naoponju.liblis.entity.UserEntity

interface UserService {
    fun findByEmail(email: String): UserDto?

    fun findEntityByEmail(email: String): UserEntity?
    fun findEntityByGoogleId(googleId: String): UserEntity?
    fun findEntityByGithubId(githubId: String): UserEntity?
    fun findEntityByAppleId(appleId: String): UserEntity?
}