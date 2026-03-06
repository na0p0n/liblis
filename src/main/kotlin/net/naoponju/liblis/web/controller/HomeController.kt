package net.naoponju.liblis.web.controller

import net.naoponju.liblis.application.service.BookService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
@Suppress("FunctionOnlyReturningConstant")
class HomeController(
    private val bookService: BookService,
) {
    @GetMapping("/")
    fun home(model: Model): String {
        val bookCount = bookService.getBookCount()

        model.addAttribute("allBookCount", bookCount)
        return "home"
    }
}
