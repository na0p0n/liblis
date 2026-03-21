package net.naoponju.liblis.infra.repository.impl

import net.naoponju.liblis.application.dto.UserBooksDto
import net.naoponju.liblis.domain.entity.UserBooksEntity
import net.naoponju.liblis.domain.repository.UserBooksRepository
import net.naoponju.liblis.infra.mapper.UserBooksMapper
import org.apache.ibatis.exceptions.PersistenceException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.UUID

@Repository
class UserBooksRepositoryImpl(
    private val userBooksMapper: UserBooksMapper,
) : UserBooksRepository {
    override fun getUserBooksList(userId: UUID): List<UserBooksDto>? {
        val userBooksEntityList = userBooksMapper.findBooksByUserId(userId)

        val userBooksDtoList =
            userBooksEntityList?.map { userBooksEntity ->
                UserBooksDto(
                    id = userBooksEntity.id,
                    userId = userBooksEntity.userId,
                    bookId = userBooksEntity.bookId,
                    status = userBooksEntity.status,
                    purchasePrice = userBooksEntity.purchasePrice,
                    purchaseDate = userBooksEntity.purchaseDate,
                )
            }?.toList()
        return userBooksDtoList
    }

    override fun getRecentAddedBooks(): List<UUID> {
        return userBooksMapper.fetchRecentAddedBooks()
    }

    /**
     * Checks whether a user-book association exists for the given user and book.
     *
     * @param userId The UUID of the user.
     * @param bookId The UUID of the book.
     * @return `true` if an association exists for the given user and book, `false` otherwise.
     */
    override fun existsByUserIdAndBookId(
        userId: UUID,
        bookId: UUID,
    ): Boolean {
        return userBooksMapper.existsByUserIdAndBookId(userId, bookId)
    }

    /**
     * Finds the UserBooks record ID for the specified user and book.
     *
     * @return the `UUID` of the matching UserBooks record if present, `null` otherwise.
     */
    override fun fetchUserBooksIdFromUserIdAndBookId(
        userId: UUID,
        bookId: UUID,
    ): UUID? {
        return userBooksMapper.fetchUserBooksIdFromUserIdAndBookId(userId, bookId)
    }

    /**
     * Determines whether a user-book relationship with the given user ID and user-books ID exists.
     *
     * @param userId The user's UUID.
     * @param userBooksId The user-books record UUID.
     * @return `true` if a matching record exists, `false` otherwise.
     */
    override fun existsByUserIdAndUserBooksId(
        userId: UUID,
        userBooksId: UUID,
    ): Boolean {
        return userBooksMapper.existsByUserIdAndUserBooksId(userId, userBooksId)
    }

    /**
     * Retrieve the user's book record for a specific book.
     *
     * Maps the persisted entity to a UserBooksDto containing id, userId, bookId, status, purchasePrice, and purchaseDate.
     *
     * @param userId The user's UUID.
     * @param bookId The book's UUID.
     * @return The matching UserBooksDto if found, `null` otherwise.
     */
    override fun findUserBookByBookId(
        userId: UUID,
        bookId: UUID,
    ): UserBooksDto? {
        return userBooksMapper.findByUserIdAndBookId(userId, bookId)?.let { entity ->
            UserBooksDto(
                id = entity.id,
                userId = entity.userId,
                bookId = entity.bookId,
                status = entity.status,
                purchasePrice = entity.purchasePrice,
                purchaseDate = entity.purchaseDate,
            )
        }
    }

    /**
     * Inserts a new user-book record for the given DTO and returns the created record's id.
     *
     * Creates and persists a new UserBooksEntity populated from the provided DTO. If a record for
     * the same userId and bookId already exists, if the insert does not affect exactly one row, or
     * if a persistence/SQL error occurs, the method returns `null`.
     *
     * @param userBooksDto DTO containing the data to store for the new user-book record.
     * @return The generated `UUID` of the inserted record, or `null` if insertion was skipped or failed.
     */
    @Suppress("ReturnCount")
    override fun insertUserBooksData(userBooksDto: UserBooksDto): UUID? {
        if (existsByUserIdAndBookId(userBooksDto.userId, userBooksDto.bookId)) {
            logger.warn("ユーザー書庫書籍登録API: 既に登録済みのためスキップ userId=${userBooksDto.userId}, bookId=${userBooksDto.bookId}")
            return null
        }
        try {
            val userBook =
                UserBooksEntity(
                    id = UUID.randomUUID(),
                    userId = userBooksDto.userId,
                    bookId = userBooksDto.bookId,
                    status = userBooksDto.status,
                    purchasePrice = userBooksDto.purchasePrice,
                    purchaseDate = userBooksDto.purchaseDate,
                    isDeleted = false,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                )
            val affected = userBooksMapper.insert(userBook)
            if (affected != 1) {
                logger.warn("ユーザー書庫書籍登録API: 反映件数が不正です affected=$affected, id=${userBook.id}")
                return null
            }
            logger.info("ユーザー書庫書籍登録API: 登録成功 id=${userBook.id}")

            return userBook.id
        } catch (e: PersistenceException) {
            logger.error("ユーザー書庫書籍登録API: MyBatisの処理で例外発生: ${e.message}")
            return null
        } catch (e: SQLException) {
            logger.error("ユーザー書庫書籍登録API: SQLで例外発生: ${e.message}")
            return null
        }
    }

    @Suppress("ThrowingNewInstanceOfSameException")
    override fun editUserBooksData(userBooksDto: UserBooksDto) {
        try {
            userBooksMapper.updateUserBooksData(userBooksDto)
            logger.info("ユーザー書庫書籍情報変更API: 情報変更に成功 id=${userBooksDto.id}")
        } catch (e: PersistenceException) {
            logger.error("ユーザー書庫書籍情報変更API: MyBatisの処理で例外発生: ${e.message}")
            throw PersistenceException(e)
        } catch (e: SQLException) {
            logger.error("ユーザー書庫書籍情報変更API: SQLで例外発生: ${e.message}")
            throw SQLException(e)
        }
    }

    override fun deleteUserBooks(userBooksId: UUID) {
        try {
            userBooksMapper.deleteUserBooks(userBooksId)
            logger.info("ユーザー書庫書籍削除API: 削除成功 id=$userBooksId")
        } catch (e: PersistenceException) {
            logger.error("ユーザー書庫書籍削除API: MyBatisの処理で例外発生:", e)
            throw e
        } catch (e: SQLException) {
            logger.error("ユーザー書庫書籍削除API: SQLで例外発生:", e)
            throw e
        }
    }

    override fun countUserBooks(userId: UUID): Int {
        return userBooksMapper.countUserBooks(userId)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserBooksRepositoryImpl::class.java)
    }
}
