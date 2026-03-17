package net.naoponju.liblis.web.controller

import net.naoponju.liblis.application.dto.LibraryBookDto
import net.naoponju.liblis.application.service.BookService
import net.naoponju.liblis.application.service.UserBooksService
import net.naoponju.liblis.application.service.UserService
import net.naoponju.liblis.common.constraint.PagingConstants
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import kotlin.math.ceil

@Controller
@Suppress("FunctionOnlyReturningConstant")
@RequestMapping("/library")
class UserBookViewController(
    private val userService: UserService,
    private val userBooksService: UserBooksService,
    private val bookService: BookService,
) {
    @GetMapping("")
    @Suppress("ReturnCount", "CyclomaticComplexMethod")
    fun showUserBooks(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @AuthenticationPrincipal userDetails: Any?,
        model: Model,
    ): String {
        val pageSize =
            if (size in PagingConstants.ALLOWED_PAGE_SIZES) {
                size
            } else {
                PagingConstants.DEFAULT_PAGE_SIZE
            }

        val email =
            when (userDetails) {
                is UserDetails -> userDetails.username
                is OAuth2User -> userDetails.attributes["email"]?.toString()
                else -> null
            } ?: return "redirect:/login"

        val userId = userService.findByEmail(email)?.id ?: return "redirect:/login"

        val totalCount = userBooksService.countUserBooks(userId)
        val totalPages = ceil(totalCount.toDouble() / pageSize).toInt().coerceAtLeast(1)

        if (page < 1 || page > totalPages) {
            return "redirect:/library?page=1"
        }

        val myBooks =
            run {
                val books = bookService.getHavingBooks(userId) ?: emptyList()
                val userBooksList = userBooksService.getUserHavingBooks(userId) ?: emptyList()
                val userBooksMap = userBooksList.associateBy { it.bookId }

                books.mapNotNull { book ->
                    val bookId = book.id ?: return@mapNotNull null
                    val ub = userBooksMap[bookId] ?: return@mapNotNull null
                    val userBooksId = ub.id ?: return@mapNotNull null
                    LibraryBookDto(
                        userBooksId = userBooksId,
                        bookId = bookId,
                        title = book.title,
                        author = book.author,
                        publisher = book.publisher,
                        publishDate = book.publishDate,
                        pages = book.pages,
                        isbn10 = book.isbn10,
                        isbn13 = book.isbn13,
                        thumbnailUrl = book.thumbnailUrl,
                        status = ub.status,
                        purchasePrice = ub.purchasePrice,
                        purchaseDate = ub.purchaseDate,
                    )
                }
            }

        model.addAttribute("myBooks", myBooks)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", totalPages)
        model.addAttribute("totalCount", totalCount)
        return "books/library"
    }
}
