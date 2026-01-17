package net.naoponju.liblis.mapper

import net.naoponju.liblis.config.UUIDTypeHandler
import net.naoponju.liblis.entity.User
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Result
import org.apache.ibatis.annotations.Results
import org.apache.ibatis.annotations.Select

@Mapper
interface UserMapper {
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
    fun findByEmail(mailAddress: String): User?
}