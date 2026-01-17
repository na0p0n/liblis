package net.naoponju.liblis.mapper

import net.naoponju.liblis.config.UUIDTypeHandler
import net.naoponju.liblis.entity.UserEntity
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Result
import org.apache.ibatis.annotations.ResultMap
import org.apache.ibatis.annotations.Results
import org.apache.ibatis.annotations.Select

@Mapper
interface UserMapper {

    // メールアドレスから検索
    @Select("""
        SELECT
          id
          , display_name
          , mail_address
          , password_hash
          , role
          , is_deleted
        FROM users 
        WHERE mail_address = #{mailAddress}
        AND is_deleted = false
    """)
    @Results(id = "userResult", value = [
        Result(id = true, column = "id", property = "id", typeHandler = UUIDTypeHandler::class),
        Result(column = "display_name", property = "displayName"),
        Result(column = "mail_address", property = "mailAddress"),
        Result(column = "password_hash", property = "passwordHash"),
        Result(column = "role", property = "role"),
        Result(column = "is_deleted", property = "isDeleted")
    ])
    fun findByEmail(mailAddress: String): UserEntity?

    // Googleの認証情報から検索
    @Select("""
        SELECT
          id
          , display_name
          , mail_address
          , password_hash
          , role
          , is_deleted
        FROM users 
        WHERE google_auth = #{googleCredential}
        AND is_deleted = false
    """)
    @ResultMap("userResult")
    fun findByGoogleCredential(googleCredential: String): UserEntity?

    // GitHubの認証情報から検索
    @Select("""
        SELECT
          id
          , display_name
          , mail_address
          , password_hash
          , role
          , is_deleted
        FROM users 
        WHERE github_auth = #{githubCredential}
        AND is_deleted = false
    """)
    @ResultMap("userResult")
    fun findByGitHubCredential(githubCredential: String): UserEntity?

    // Appleの認証情報から検索
    @Select("""
        SELECT
          id
          , display_name
          , mail_address
          , password_hash
          , role
          , is_deleted
        FROM users 
        WHERE Apple_auth = #{appleCredential}
        AND is_deleted = false
    """)
    @ResultMap("userResult")
    fun findByAppleCredential(appleCredential: String): UserEntity?
}