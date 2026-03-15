package net.naoponju.liblis.web.controller

import net.naoponju.liblis.application.dto.LibraryBookDto
import net.naoponju.liblis.application.service.BookService
import net.naoponju.liblis.application.service.UserBooksService
import net.naoponju.liblis.application.service.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@Suppress("FunctionOnlyReturningConstant")
@RequestMapping("/library")
class UserBookViewController(
    private val userService: UserService,
    private val userBooksService: UserBooksService,
    private val bookService: BookService,
) {
    @GetMapping("")
    fun showUserBooks(
        @AuthenticationPrincipal userDetails: UserDetails,
        model: Model,
    ): String {
        val userId = userService.findByEmail(userDetails.username)?.id

        val myBooks =
            if (userId != null) {
                val books = bookService.getHavingBooks(userId) ?: emptyList()
                val userBooksList = userBooksService.getUserHavingBooks(userId) ?: emptyList()
                val userBooksMap = userBooksList.associateBy { it.bookId }

                books.mapNotNull { book ->
                    val ub = book.id?.let { userBooksMap[it] } ?: return@mapNotNull null
                    LibraryBookDto(
                        userBooksId = ub.id!!,
                        bookId = book.id,
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
            } else {
                emptyList()
            }

        model.addAttribute("myBooks", myBooks)
        return "books/library"
    }
}
