package net.naoponju.liblis.application.service

import net.naoponju.liblis.domain.entity.BookEntity
import net.naoponju.liblis.domain.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

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

    fun findBookListByBookIds(bookIds: List<UUID>): List<BookEntity> {
        if (bookIds.isEmpty()) {
            return emptyList()
        }
        val bookList = bookRepository.findBookListByBookIdList(bookIds)

        return bookList
    }

    fun getBookList(): List<BookEntity> {
        val foundFromDB = bookRepository.fetchAllBooks()

        return foundFromDB
    }

    fun getAllBookCount(): Int {
        val countBooks = bookRepository.countAllBooks()

        return countBooks
    }

    fun getHavingBooks(userId: UUID): List<BookEntity>? {
        val havingBooks = bookRepository.fetchUserHavingBooks(userId)

        return havingBooks
    }

    fun getBookListPaged(
        offset: Int,
        limit: Int,
    ): List<BookEntity> {
        return bookRepository.findAllPaged(offset, limit)
    }

    fun getHavingBookCount(userId: UUID): Int? {
        val countBooks = bookRepository.fetchUserHavingBooks(userId).size

        return countBooks
    }
}
