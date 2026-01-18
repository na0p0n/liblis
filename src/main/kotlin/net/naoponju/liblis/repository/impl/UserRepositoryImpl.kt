package net.naoponju.liblis.repository.impl

import net.naoponju.liblis.entity.UserEntity
import net.naoponju.liblis.mapper.UserMapper
import net.naoponju.liblis.repository.UserRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(private val userMapper: UserMapper): UserRepository {
    override fun findByEmail(email: String) = userMapper.findByEmail(email)
    override fun findByGoogleCredential(googleId: String) = userMapper.findByGoogleCredential(googleId)
    override fun findByGitHubCredential(githubId: String) = userMapper.findByGitHubCredential(githubId)
    override fun findByAppleCredential(appleId: String) = userMapper.findByAppleCredential(appleId)
    override fun save(user: UserEntity) = userMapper.insert(user)
}