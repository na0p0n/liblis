package net.naoponju.liblis.web.controller

import net.naoponju.liblis.application.service.BookService
import net.naoponju.liblis.application.service.UserService
import net.naoponju.liblis.common.constraint.PagingConstants
import net.naoponju.liblis.common.exception.BookNotFoundException
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
@RequestMapping("/books")
class BookController(
    private val bookService: BookService,
    private val userService: UserService,
) {
    @Suppress("ReturnCount")
    @GetMapping("/list")
    fun showBookList(
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

        val totalCount = bookService.getAllBookCount()
        val totalPages = ceil(totalCount.toDouble() / pageSize).toInt().coerceAtLeast(1)

        if (page < 1 || page > totalPages) {
            return "redirect:/books/list?page=1"
        }

        val offset = (page - 1) * pageSize
        val books =
            try {
                bookService.getBookListPaged(offset, pageSize)
            } catch (_: BookNotFoundException) {
                emptyList()
            }

        val userId = userService.findByEmail(email)?.id

        val ownedBookIds =
            userId
                ?.let { bookService.getHavingBooks(it) }
                ?.mapNotNull { it.id }
                ?.toHashSet()
                ?: HashSet()

        model.addAttribute("books", books)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("ownedBookIds", ownedBookIds)
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", totalPages)
        model.addAttribute("totalCount", totalCount)
        return "books/list"
    }

    @GetMapping("/register")
    fun showBookRegister(): String {
        return "books/register"
    }
}
