package net.naoponju.liblis.application.service

import net.naoponju.liblis.common.exception.BookNotFoundException
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
            val foundFromRakuten =
                bookRepository.findBookByISBNFromRakuten(isbn)
                    ?: throw BookNotFoundException("書籍が見つかりませんでした: isbn=$isbn")
            bookRepository.insert(foundFromRakuten)
            return foundFromRakuten to false
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

    fun fetchUserHavingBookIdsInBookIdList(
        userId: UUID,
        bookIds: List<UUID>,
    ): List<BookEntity>? {
        if (bookIds.isEmpty()) return emptyList()
        return bookRepository.fetchUserHavingBookIdsInBookIdList(
            userId,
            bookIds,
        )
    }

    fun findByTitle(title: String): List<BookEntity> =
        bookRepository.findBookByTitle(title)

    fun findByAuthor(author: String): List<BookEntity> =
        bookRepository.findBookByAuthor(author)

    fun findUserBooksByTitle(userId: UUID, title: String): List<BookEntity> =
        bookRepository.findUserBooksByTitle(userId, title)

    fun findUserBooksByAuthor(userId: UUID, author: String): List<BookEntity> =
        bookRepository.findUserBooksByAuthor(userId, author)

    fun getHavingBooksPaged(
        userId: UUID,
        offset: Int,
        limit: Int,
    ): List<BookEntity>? {
        val havingBooks =
            bookRepository.fetchUserHavingBooksPaged(
                userId,
                offset,
                limit,
            )

        return havingBooks
    }

    fun getBookListPaged(
        offset: Int,
        limit: Int,
    ): List<BookEntity> {
        return bookRepository.findAllPaged(offset, limit)
    }

    // B-9: 書籍詳細ページ用
    fun findBookById(id: UUID): BookEntity? {
        return bookRepository.findById(id)
    }
}
