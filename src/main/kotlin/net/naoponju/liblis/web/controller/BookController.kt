package net.naoponju.liblis.web.controller

import net.naoponju.liblis.application.service.BookService
import net.naoponju.liblis.common.exception.BookNotFoundException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@Suppress("FunctionOnlyReturningConstant")
@RequestMapping("/books")
class BookController(
    private val bookService: BookService,
) {
    @GetMapping("/list")
    fun showBookList(model: Model): String {
        val books =
            try {
                bookService.getBookList()
            } catch (_: BookNotFoundException) {
                emptyList()
            }

        model.addAttribute("books", books)
        return "books/list"
    }

    @GetMapping("/register")
    fun showBookRegister(): String {
        return "books/register"
    }
}
