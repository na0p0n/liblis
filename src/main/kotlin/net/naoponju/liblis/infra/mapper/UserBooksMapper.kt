package net.naoponju.liblis.infra.mapper

import net.naoponju.liblis.application.dto.UserBooksDto
import net.naoponju.liblis.common.config.UUIDTypeHandler
import net.naoponju.liblis.domain.entity.UserBooksEntity
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Result
import org.apache.ibatis.annotations.Results
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import java.util.UUID

@Mapper
@Suppress("TooManyFunctions")
interface UserBooksMapper {
    @Select(
        """
            SELECT
                id
                , user_id
                , book_id
                , status
                , purchase_price
                , purchase_date
                , is_deleted
                , created_at
                , updated_at
            FROM user_books
            WHERE user_id = #{userId, jdbcType=OTHER}
            AND is_deleted = false
        """,
    )
    @Results(
        id = "userBooksResult",
        value = [
            Result(id = true, column = "id", property = "id", typeHandler = UUIDTypeHandler::class),
            Result(column = "user_id", property = "userId", typeHandler = UUIDTypeHandler::class),
            Result(column = "book_id", property = "bookId", typeHandler = UUIDTypeHandler::class),
            Result(column = "status", property = "status"),
            Result(column = "purchase_price", property = "purchasePrice"),
            Result(column = "purchase_date", property = "purchaseDate", javaType = java.time.LocalDate::class),
            Result(column = "is_deleted", property = "isDeleted"),
            Result(column = "created_at", property = "createdAt"),
            Result(column = "updated_at", property = "updatedAt"),
        ],
    )
    fun findBooksByUserId(userId: UUID): List<UserBooksEntity>?

    @Select(
        """
        SELECT COUNT(*) > 0
        FROM user_books
        WHERE user_id = #{userId, jdbcType=OTHER}
        AND book_id = #{bookId, jdbcType=OTHER}
        AND is_deleted = false;
    """,
    )
    fun existsByUserIdAndBookId(
        userId: UUID,
        bookId: UUID,
    ): Boolean

    @Select(
        """
        SELECT
            COUNT(*) > 0
        FROM user_books
        WHERE id = #{userBooksId, jdbcType=OTHER}
        AND user_id = #{userId, jdbcType=OTHER}
        AND is_deleted = false;
    """,
    )
    fun existsByUserIdAndUserBooksId(
        userId: UUID,
        userBooksId: UUID,
    ): Boolean

    @Insert(
        """
        INSERT INTO user_books (
            id
            , user_id
            , book_id
            , status
            , purchase_price
            , purchase_date
            , is_deleted
            , created_at
            , updated_at
        ) VALUES (
            #{id, jdbcType=OTHER}
            , #{userId, jdbcType=OTHER}
            , #{bookId, jdbcType=OTHER}
            , #{status}
            , #{purchasePrice}
            , #{purchaseDate}
            , #{isDeleted}
            , CURRENT_TIMESTAMP
            , CURRENT_TIMESTAMP
        );
    """,
    )
    fun insert(userBooksInfo: UserBooksEntity): Int

    @Update(
        """
            UPDATE user_books SET
                status = #{status},
                purchase_price = #{purchasePrice},
                purchase_date = #{purchaseDate},
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id, jdbcType=OTHER}
        """,
    )
    fun updateUserBooksData(userBooksInfo: UserBooksDto)

    @Update(
        """
        UPDATE user_books SET
            status = '',
            is_deleted = true,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id, jdbcType=OTHER}
    """,
    )
    fun deleteUserBooks(id: UUID)

    @Select(
        """
            SELECT COUNT(*) FROM user_books WHERE user_id = #{userId, jdbcType=OTHER} AND is_deleted = false;
        """,
    )
    fun countUserBooks(userId: UUID): Int
}
