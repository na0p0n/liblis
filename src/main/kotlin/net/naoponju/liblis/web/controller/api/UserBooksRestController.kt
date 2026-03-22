package net.naoponju.liblis.web.controller.api

import net.naoponju.liblis.application.dto.UserBooksDto
import net.naoponju.liblis.application.dto.UserBooksForm
import net.naoponju.liblis.application.dto.UserBooksUpdateForm
import net.naoponju.liblis.application.service.UserBooksService
import net.naoponju.liblis.application.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@Suppress("LongMethod", "TooGenericExceptionCaught")
@RestController
@RequestMapping("/api/library")
class UserBooksRestController(
    private val userBooksService: UserBooksService,
    private val userService: UserService,
) {
    @GetMapping("/list")
    @Suppress("ReturnCount")
    fun getUserBookList(
        @AuthenticationPrincipal userDetails: Any?,
    ): ResponseEntity<List<UserBooksDto>?> {
        val email =
            when (userDetails) {
                is UserDetails -> userDetails.username
                is OAuth2User -> userDetails.attributes["email"]?.toString()
                else -> null
            } ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val userId =
            userService.findByEmail(email)?.id
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val result = userBooksService.getUserHavingBooks(userId)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/recent")
    fun getRecentAddedBookList(): ResponseEntity<List<UUID>> {
        val bookIdList = userBooksService.getRecentAddedBooks()
        return ResponseEntity.ok(bookIdList)
    }

    @PostMapping("/add")
    @Suppress("ReturnCount")
    fun addUserBooks(
        @AuthenticationPrincipal userDetails: Any?,
        @ModelAttribute form: UserBooksForm,
    ): ResponseEntity<Any> {
        val email =
            when (userDetails) {
                is UserDetails -> userDetails.username
                is OAuth2User -> userDetails.attributes["email"]?.toString()
                else -> null
            } ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val userId =
            userService.findByEmail(email = email)?.id
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (form.purchaseYear == null || form.purchaseMonth == null || form.purchaseDay == null) {
            return ResponseEntity.badRequest().build()
        }

        val purchaseDate =
            runCatching {
                LocalDate.of(
                    form.purchaseYear,
                    form.purchaseMonth,
                    form.purchaseDay,
                )
            }.getOrElse {
                return ResponseEntity.badRequest().build()
            }

        // 過去にuser_booksに登録された本かチェック
        val userBooksIdCreated =
            userBooksService.getUserBooksIdFromUserIdAndBookId(
                userId,
                form.bookId,
            )

        if (userBooksIdCreated != null) {
            val userBooksData =
                UserBooksDto(
                    id = userBooksIdCreated,
                    userId = userId,
                    bookId = form.bookId,
                    status = form.status,
                    purchaseDate = purchaseDate,
                    purchasePrice = form.purchasePrice,
                )
            try {
                userBooksService.updateUserBooksData(userBooksData)
                return ResponseEntity.ok().build()
            } catch (e: Exception) {
                logger.warn("ユーザー書庫書籍登録API: ユーザーの書庫に過去登録あり・書庫情報アップデートに失敗: {}", e.message)
                return ResponseEntity.badRequest().build()
            }
        } else {
            val userBooksData =
                UserBooksDto(
                    id = null,
                    userId = userId,
                    bookId = form.bookId,
                    status = form.status,
                    purchaseDate = purchaseDate,
                    purchasePrice = form.purchasePrice,
                )
            logger.debug(
                "取得したデータ: bookId={}, status={}",
                form.bookId,
                form.status,
            )
            try {
                val result = userBooksService.insertUserBooksData(userBooksData)
                logger.info("ユーザー書庫書籍登録API: 書庫情報登録に成功: ID=$result")
                return ResponseEntity.ok().build()
            } catch (e: Exception) {
                logger.warn("ユーザー書庫書籍登録API: 書庫情報登録に失敗: {}", e.message)
                return ResponseEntity.badRequest().build()
            }
        }
    }

    @Suppress("ReturnCount")
    @PutMapping("/{userBooksId}")
    fun updateUserBooks(
        @AuthenticationPrincipal userDetails: Any?,
        @PathVariable userBooksId: UUID,
        @ModelAttribute form: UserBooksUpdateForm,
    ): ResponseEntity<Unit> {
        val email =
            when (userDetails) {
                is UserDetails -> userDetails.username
                is OAuth2User -> userDetails.attributes["email"]?.toString()
                else -> null
            } ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val userId =
            userService.findByEmail(email)?.id
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!userBooksService.isOwnedByUser(userId, userBooksId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        userBooksService.updateUserBooksData(
            UserBooksDto(
                id = userBooksId,
                userId = userId,
                bookId = form.bookId,
                status = form.status,
                purchasePrice = form.purchasePrice,
                purchaseDate = form.purchaseDate,
            ),
        )
        return ResponseEntity.ok().build()
    }

    @Suppress("ReturnCount")
    @DeleteMapping("/{userBooksId}")
    fun deleteUserBooks(
        @AuthenticationPrincipal userDetails: Any?,
        @PathVariable userBooksId: UUID,
    ): ResponseEntity<Unit> {
        val email =
            when (userDetails) {
                is UserDetails -> userDetails.username
                is OAuth2User -> userDetails.attributes["email"]?.toString()
                else -> null
            } ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val userId =
            userService.findByEmail(email)?.id
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!userBooksService.isOwnedByUser(userId, userBooksId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        userBooksService.deleteUserBooksData(userBooksId)
        return ResponseEntity.noContent().build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserBooksRestController::class.java)
    }
}
