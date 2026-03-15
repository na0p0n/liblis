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

@RestController
@RequestMapping("/api/library")
class UserBooksRestController(
    private val userBooksService: UserBooksService,
    private val userService: UserService,
) {
    @GetMapping("/list")
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
                ?: return ResponseEntity.badRequest().build()

        val result = userBooksService.getUserHavingBooks(userId)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/add")
    fun addUserBooks(
        @AuthenticationPrincipal userDetails: Any?,
        @ModelAttribute form: UserBooksForm,
    ): ResponseEntity<UUID>? {
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

        val purchaseDate = runCatching {
            LocalDate.of(
                form.purchaseYear,
                form.purchaseMonth,
                form.purchaseDay,
            )
        }.getOrElse {
            return ResponseEntity.badRequest().build()
        }

        val userBooksData =
            UserBooksDto(
                id = null,
                userId = userId,
                bookId = form.bookId,
                status = form.status,
                purchaseDate = purchaseDate,
                purchasePrice = form.purchasePrice,
            )
        logger.info("取得したデータ: $userBooksData")
        val result = userBooksService.insertUserBooksData(userBooksData)

        return if (result == null) {
            ResponseEntity.badRequest().build()
        } else {
            ResponseEntity.ok(result)
        }
    }

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
                ?: return ResponseEntity.badRequest().build()

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
                ?: return ResponseEntity.badRequest().build()

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
