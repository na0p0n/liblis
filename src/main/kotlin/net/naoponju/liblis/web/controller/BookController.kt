package net.naoponju.liblis.web.controller

import net.naoponju.liblis.application.service.BookService
import net.naoponju.liblis.application.service.UserBooksService
import net.naoponju.liblis.application.service.UserService
import net.naoponju.liblis.common.constraint.PagingConstants
import net.naoponju.liblis.common.exception.BookNotFoundException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.UUID
import kotlin.math.ceil

@Controller
@Suppress("FunctionOnlyReturningConstant")
@RequestMapping("/books")
class BookController(
    private val bookService: BookService,
    private val userBooksService: UserBooksService,
    private val userService: UserService,
) {
    /**
     * Displays a paginated list of books and marks which books are owned by the authenticated user.
     *
     * Validates the requested page and page size, redirects to login if the authenticated user's email
     * cannot be determined, and adds pagination data and ownership information to the provided model.
     *
     * @param page 1-based page number to display.
     * @param size Desired page size; if not allowed, a default page size is used.
     * @param userDetails Authentication principal (may be a Spring `UserDetails` or an `OAuth2User`) used to resolve the user's email.
     * @param model Model to receive attributes required by the books list view.
     * @return The view name "books/list", or a redirect string to "/login" or "/books/list?page=1" when validation fails.
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

        val bookIds =
            books.map { book ->
                book.id!!
            }.toList()

        val userId = userService.findByEmail(email)?.id

        val ownedBookIds =
            userId
                ?.let { bookService.fetchUserHavingBookIdsInBookIdList(it, bookIds) }
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

    /**
     * Render the book detail page for the specified book and the current authenticated user.
     *
     * Determines a return target based on the `from` parameter, extracts the authenticated
     * user's email from either a Spring `UserDetails` or an `OAuth2User` (redirects to `/login`
     * when absent), loads the requested book (adds a flash error and redirects to `/books/list`
     * when not found), resolves the user's relation to the book, and populates the model with
     * `book`, `userBook`, `redirectUrl`, and `redirectText`.
     *
     * @param bookId UUID of the requested book.
     * @param from Source page identifier; "list" maps to `/books/list` and "library" maps to `/library`.
     * @param userDetails The authenticated principal (may be a `UserDetails`, an `OAuth2User`, or null).
     * @param model MVC model to receive view attributes.
     * @param redirectAttributes Used to add flash attributes when redirecting (e.g., error message if book not found).
     * @return The view name `books/detail` when the book is found; otherwise a redirect string to `/login`, `/books/list`, or `/error/500`.
     */
    @Suppress("ReturnCount")
    @GetMapping("/{bookId}")
    fun showBookDetail(
        @PathVariable bookId: UUID,
        @RequestParam(defaultValue = "list") from: String,
        @AuthenticationPrincipal userDetails: Any?,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        val redirectInfo =
            when (from) {
                "list" -> Pair("/books/list", "書籍一覧画面に戻る")
                "library" -> Pair("/library", "My書庫画面に戻る")
                else -> return "redirect:/error/500"
            }

        val email =
            when (userDetails) {
                is UserDetails -> userDetails.username
                is OAuth2User -> userDetails.attributes["email"]?.toString()
                else -> null
            } ?: return "redirect:/login"

        val book =
            bookService.findBookById(bookId)
                ?: run {
                    redirectAttributes.addFlashAttribute("error", "書籍が見つかりませんでした。")
                    return "redirect:/books/list"
                }

        val userId = userService.findByEmail(email)?.id
        val userBook = userId?.let { userBooksService.findUserBookByBookId(it, bookId) }

        model.addAttribute("book", book)
        model.addAttribute("userBook", userBook)
        model.addAttribute("redirectUrl", redirectInfo.first)
        model.addAttribute("redirectText", redirectInfo.second)
        return "books/detail"
    }

    /**
     * Displays the book registration page.
     *
     * @return The view name "books/register".
     */
    @GetMapping("/register")
    fun showBookRegister(): String {
        return "books/register"
    }
}
