package net.naoponju.liblis.application.service

import net.naoponju.liblis.domain.entity.BookEntity
import net.naoponju.liblis.domain.repository.BookRepository
import org.springframework.stereotype.Service

@Service
class BookService(
    private val bookRepository: BookRepository,
) {
    // ISBNから本を検索
    // DBにあるか検索し、なければGoogleBooksAPIから取得
    // Input: isbn:String
    // Output: Pair<BookEntity, isFoundFromDB(Boolean)
    fun findBookByISBN(isbn: String): Pair<BookEntity, Boolean>? {
        val foundFromDB = bookRepository.findBookByISBN(isbn)

        if (foundFromDB != null) {
            return foundFromDB to true
        } else {
            val foundFromGoogle = bookRepository.findBookByISBNFromGoogle(isbn)
            bookRepository.insert(foundFromGoogle)
            return foundFromGoogle to false
        }
    }

//    fun findBookByISBNFromWebApi(isbn: String): FoundBookDataDto? {
//        val foundBookData = bookRepository.findBookByISBNFromGoogle(isbn)
//        return foundBookData
//    }
}
