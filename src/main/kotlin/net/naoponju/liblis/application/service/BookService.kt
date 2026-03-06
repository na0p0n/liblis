package net.naoponju.liblis.application.service

import net.naoponju.liblis.common.exception.BookNotFoundException
import net.naoponju.liblis.domain.entity.BookEntity
import net.naoponju.liblis.domain.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(
    private val bookRepository: BookRepository,
) {
    /**
     *  ISBNから本を検索
     *  DBになければGoogleBooksAPIから取得
     *  Input: isbn(String)
     *  Output: Pair<BookEntity, isFoundFromDB>
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun findBookByISBN(isbn: String): Pair<BookEntity, Boolean> {
        val foundFromDB = bookRepository.findBookByISBN(isbn)

        if (foundFromDB != null) {
            return foundFromDB to true
        } else {
            val foundFromGoogle = bookRepository.findBookByISBNFromGoogle(isbn)
            bookRepository.insert(foundFromGoogle)
            return foundFromGoogle to false
        }
    }

    fun getBookList(): List<BookEntity> {
        val foundFromDB = bookRepository.fetchAllBooks()

        if (foundFromDB != null) {
            return foundFromDB
        } else {
            throw BookNotFoundException("書籍DBに書籍がありません。")
        }
    }
}
