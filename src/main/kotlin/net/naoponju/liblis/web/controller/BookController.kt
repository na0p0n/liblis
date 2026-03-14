package net.naoponju.liblis.web.controller

import net.naoponju.liblis.application.service.BookService
import net.naoponju.liblis.application.service.UserService
import net.naoponju.liblis.common.exception.BookNotFoundException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.util.UUID

@Controller
@Suppress("FunctionOnlyReturningConstant")
@RequestMapping("/books")
class BookController(
    private val bookService: BookService,
    private val userService: UserService,
) {
    @GetMapping("/list")
    fun showBookList(
        @AuthenticationPrincipal userDetails: UserDetails,
        model: Model,
    ): String {
        val books =
            try {
                bookService.getBookList()
            } catch (_: BookNotFoundException) {
                emptyList()
            }

        val email = userDetails.username
        val userId = email?.let { userService.findByEmail(it)?.id }

        val ownedBookIds =
            userId
                ?.let { bookService.getHavingBooks(it) }
                ?.mapNotNull { it.id }
                ?.toSet()
                ?: emptySet<UUID>()
        model.addAttribute("userId", userId)
        model.addAttribute("books", books)
        model.addAttribute("ownedBookIds", ownedBookIds)
        return "books/list"
    }

    @GetMapping("/register")
    fun showBookRegister(): String {
        return "books/register"
    }
}
