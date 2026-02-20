package net.naoponju.liblis.infra.repository.impl

import net.naoponju.liblis.domain.entity.UserEntity
import net.naoponju.liblis.infra.mapper.UserMapper
import net.naoponju.liblis.infra.repository.UserRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepositoryImpl(private val userMapper: UserMapper): UserRepository {
    override fun findByEmail(email: String) = userMapper.findByEmail(email)
    override fun findByGoogleCredential(googleId: String) = userMapper.findByGoogleCredential(googleId)
    override fun findByGitHubCredential(githubId: String) = userMapper.findByGitHubCredential(githubId)
    override fun findByAppleCredential(appleId: String) = userMapper.findByAppleCredential(appleId)

    override fun save(user: UserEntity) = userMapper.insert(user)

    override fun addGoogleCredentialById(id: UUID, googleId: String) = userMapper.updateGoogleCredential(id, googleId)
    override fun addGithubCredentialById(id: UUID, githubId: String) = userMapper.updateGithubCredential(id, githubId)
    override fun addAppleCredentialById(id: UUID, appleId: String) = userMapper.updateAppleCredential(id, appleId)

    override fun clearGoogleCredentialByMailAddress(email: String) = userMapper.clearGoogleCredential(email)
    override fun clearGithubCredentialByMailAddress(email: String) = userMapper.clearGithubCredential(email)
    override fun clearAppleCredentialByMailAddress(email: String) = userMapper.clearAppleCredential(email)
}
