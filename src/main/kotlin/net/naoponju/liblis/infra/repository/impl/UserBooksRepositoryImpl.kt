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

    @Suppress("ReturnCount")
    override fun insertUserBooksData(userBooksDto: UserBooksDto): UUID? {
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
            userBooksMapper.insert(userBook)
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

    override fun editUserBooksData(userBooksDto: UserBooksDto) {
        try {
            userBooksMapper.updateUserBooksData(userBooksDto)
            logger.info("ユーザー書庫書籍情報変更API: 情報変更に成功 id=${userBooksDto.id}")
        } catch (e: PersistenceException) {
            logger.error("ユーザー書庫書籍情報変更API: MyBatisの処理で例外発生: ${e.message}")
        } catch (e: SQLException) {
            logger.error("ユーザー書庫書籍情報変更API: SQLで例外発生: ${e.message}")
        }
    }

    override fun deleteUserBooks(userBooksId: UUID) {
        try {
            userBooksMapper.deleteUserBooks(userBooksId)
            logger.info("ユーザー書庫書籍削除API: 登録成功 id=$userBooksId")
        } catch (e: PersistenceException) {
            logger.error("ユーザー書庫書籍削除API: MyBatisの処理で例外発生: ${e.message}")
        } catch (e: SQLException) {
            logger.error("ユーザー書庫書籍削除API: SQLで例外発生: ${e.message}")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserBooksRepositoryImpl::class.java)
    }
}
