package net.naoponju.liblis.application.service

import net.naoponju.liblis.application.dto.FoundBookDataDto
import net.naoponju.liblis.domain.repository.BookRepository
import org.springframework.stereotype.Service

@Service
class BookService(
    private val bookRepository: BookRepository,
) {

    fun findBookByISBNFromWebApi(isbn: String): FoundBookDataDto? {
        val foundBookData = bookRepository.findBookByISBNFromGoogle(isbn)
        return foundBookData
    }
}