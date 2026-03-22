package net.naoponju.liblis.web.controller.api

import net.naoponju.liblis.application.service.BookService
import net.naoponju.liblis.common.exception.BookNotFoundException
import net.naoponju.liblis.common.exception.RemoteApiServiceException
import net.naoponju.liblis.domain.entity.BookEntity
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/books")
class BookRestController(
    private val bookService: BookService,
) {
    @Suppress("ReturnCount")
    @GetMapping("/find")
    fun findBookFromAPI(
        @RequestParam isbn: String,
    ): ResponseEntity<BookEntity> {
        try {
            val foundBookData = bookService.findBookByISBN(isbn)
            foundBookData.let {
                if (it.second) {
                    logger.info("書籍情報Web取得API: DBからのデータ取得に成功 (取得データ: $foundBookData)")
                } else {
                    logger.info("書籍情報Web取得API: 楽天ブックスからのデータ取得に成功 (取得データ: $foundBookData)")
                }
            }

            return ResponseEntity.ok(foundBookData.first)
        } catch (e: BookNotFoundException) {
            logger.error("書籍情報Web取得API: データ取得に失敗: ${e.message}")
            return ResponseEntity.notFound().build()
        } catch (e: RemoteApiServiceException) {
            logger.error("書籍情報Web取得API: API実行時にエラー: ${e.message}")
            return ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/list")
    fun getBookList(): ResponseEntity<List<BookEntity>> {
        try {
            val bookList = bookService.getBookList()
            return ResponseEntity.ok(bookList)
        } catch (e: BookNotFoundException) {
            logger.error("書籍リスト取得API: エラー: ${e.message}")
            return ResponseEntity.notFound().build()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BookRestController::class.java)
    }
}
