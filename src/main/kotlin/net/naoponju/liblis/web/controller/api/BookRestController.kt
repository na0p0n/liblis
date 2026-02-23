package net.naoponju.liblis.web.controller.api

import net.naoponju.liblis.application.dto.FoundBookDataDto
import net.naoponju.liblis.application.service.BookService
import net.naoponju.liblis.common.config.LoggingAspect
import net.naoponju.liblis.common.exception.ApiKeyNotFoundException
import net.naoponju.liblis.common.exception.BookNotFoundException
import net.naoponju.liblis.common.exception.RemoteApiServiceException
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
    @GetMapping("/find")
    fun findBookFromAPI(
        @RequestParam isbn: String,
    ): ResponseEntity<FoundBookDataDto> {

        try {
            val foundBookData = bookService.findBookByISBNFromWebApi(isbn)
            logger.info("書籍情報Web取得API: Googleからのデータ取得に成功 (取得データ: $foundBookData)")

            return ResponseEntity.ok(foundBookData)
        } catch (e: BookNotFoundException) {
            logger.error("書籍情報Web取得API: データ取得に失敗: ${e.message}")
            return ResponseEntity.notFound().build()
        } catch (e: RemoteApiServiceException) {
            logger.error("書籍情報Web取得API: API実行時にエラー: ${e.message}")
            return ResponseEntity.internalServerError().build()
        } catch (e: ApiKeyNotFoundException) {
            logger.error("書籍情報Web取得API: APIエラー: ${e.message}")
            return ResponseEntity.badRequest().build()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LoggingAspect::class.java)
    }
}