package net.naoponju.liblis.web.controller

import net.naoponju.liblis.application.service.BookService
import net.naoponju.liblis.application.service.UserBooksService
import net.naoponju.liblis.application.service.UserService
import net.naoponju.liblis.common.constraint.PagingConstants
import net.naoponju.liblis.infra.mapper.RakutenBooksGenreMapper
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
@Suppress("FunctionOnlyReturningConstant", "LongParameterList", "CyclomaticComplexMethod")
@RequestMapping("/books")
class BookController(
    private val bookService: BookService,
    private val userBooksService: UserBooksService,
    private val userService: UserService,
    private val rakutenBooksGenreMapper: RakutenBooksGenreMapper,
) {
    @Suppress("ReturnCount")
    @GetMapping("/list")
    fun showBookList(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) type: String?,
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
            when {
                q.isNullOrBlank() -> bookService.getBookListPaged(offset, pageSize)
                type == "author" -> bookService.findByAuthor(q)
                else -> bookService.findByTitle(q) // デフォルト: title
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

        val genreIds =
            books
                .mapNotNull { it.category?.split("/")?.firstOrNull() }
                .distinct()

        val genreNameMap: Map<String, String> =
            if (genreIds.isNotEmpty()) {
                rakutenBooksGenreMapper.findGenreNamesByIds(genreIds)
                    .associate { it["books_genre_id"]!! to (it["display_genre_name"] ?: "") }
                    .filter { it.value.isNotEmpty() }
            } else {
                emptyMap()
            }

        model.addAttribute("genreNameMap", genreNameMap) // ★追加

        model.addAttribute("books", books)
        model.addAttribute("q", q)
        model.addAttribute("type", type)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("ownedBookIds", ownedBookIds)
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", totalPages)
        model.addAttribute("totalCount", totalCount)
        return "books/list"
    }

    // B-9: 書籍詳細ページ
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

        val genrePath =
            book.category
                ?.split("/")
                ?.firstOrNull()
                ?.let { rakutenBooksGenreMapper.findGenrePathById(it) }

        model.addAttribute("book", book)
        model.addAttribute("userBook", userBook)
        model.addAttribute("redirectUrl", redirectInfo.first)
        model.addAttribute("redirectText", redirectInfo.second)
        model.addAttribute("genrePath", genrePath)
        return "books/detail"
    }

    @GetMapping("/register")
    fun showBookRegister(): String {
        return "books/register"
    }
}
