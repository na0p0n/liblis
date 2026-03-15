package net.naoponju.liblis.web.controller.api

import net.naoponju.liblis.application.dto.UserBooksDto
import net.naoponju.liblis.application.dto.UserBooksForm
import net.naoponju.liblis.application.service.UserBooksService
import net.naoponju.liblis.application.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/library")
class UserBooksRestController(
    private val userBooksService: UserBooksService,
    private val userService: UserService,
) {
    @GetMapping("/list")
    fun getUserBookList(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): List<UserBooksDto>? {
        val email = userDetails.username
        val userId = userService.findByEmail(email = email)?.id
        val result = userId?.let { userBooksService.getUserHavingBooks(it) }
        return result
    }

    @PostMapping("/add")
    fun addUserBooks(
        @AuthenticationPrincipal userDetails: UserDetails,
        @ModelAttribute form: UserBooksForm,
    ): ResponseEntity<UUID>? {
        try {
            val email = userDetails.username
            val userId = userService.findByEmail(email = email)?.id ?: throw NoSuchFieldError("UserId Not Found.")

            val userBooksData =
                UserBooksDto(
                    id = null,
                    userId = userId,
                    bookId = form.bookId,
                    status = form.status,
                    purchaseDate =
                        LocalDate.of(
                            form.purchaseYear!!,
                            form.purchaseMonth!!,
                            form.purchaseDay!!,
                        ),
                    purchasePrice = form.purchasePrice,
                )
            logger.info("取得したデータ: $userBooksData")
            val result = userBooksService.insertUserBooksData(userBooksData)
            return if (result == null) {
                ResponseEntity.badRequest().build()
            } else {
                ResponseEntity.ok(result)
            }
        } catch (e: NoSuchFieldError) {
            logger.warn("書庫書籍登録API: userIdが見つかりません。 ${e.message}")
            return ResponseEntity.badRequest().build()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserBooksRestController::class.java)
    }
}
