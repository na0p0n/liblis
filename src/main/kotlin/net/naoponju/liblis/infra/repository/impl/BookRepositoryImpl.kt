package net.naoponju.liblis.infra.repository.impl

import net.naoponju.liblis.common.constraint.PublishDateLength
import net.naoponju.liblis.domain.entity.BookEntity
import net.naoponju.liblis.domain.repository.BookRepository
import net.naoponju.liblis.infra.api.GoogleBooksApiClient
import net.naoponju.liblis.infra.mapper.BookMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class BookRepositoryImpl(
    private val bookMapper: BookMapper,
    private val googleBooksApiClient: GoogleBooksApiClient,
) : BookRepository {
    override fun findBookByISBN(isbn: String): BookEntity? {
        return bookMapper.findByISBN(isbn)
    }

    override fun fetchAllBooks(): List<BookEntity> {
        return bookMapper.fetchAllBooksOrderByTitle() ?: emptyList()
    }

    override fun fetchUserHavingBooks(userId: UUID): List<BookEntity> {
        return bookMapper.fetchUserBooks(userId) ?: emptyList()
    }

    override fun findBookByTitle(title: String): List<BookEntity> {
        return bookMapper.findBookByTitle(title) ?: emptyList()
    }

    override fun findBookByAuthor(author: String): List<BookEntity> {
        return bookMapper.findBookByAuthor(author) ?: emptyList()
    }

    override fun fetchRecentBooks(limit: Int): List<BookEntity> {
        return bookMapper.fetchRecentBooks(limit) ?: emptyList()
    }

    override fun findAllPaged(
        offset: Int,
        limit: Int,
    ): List<BookEntity> {
        return bookMapper.findAllPaged(offset, limit)
    }

    override fun findBookByISBNFromGoogle(isbn: String): BookEntity {
        val fetchedBookData = googleBooksApiClient.fetchBookData(isbn)
        val convertedBookPublishedDate = fetchedBookData.publishedDate?.let { convertToLocalDate(it) }

        return BookEntity(
            id = UUID.randomUUID(),
            title = fetchedBookData.title,
            author = fetchedBookData.authors,
            publisher = fetchedBookData.publisher,
            publishDate = convertedBookPublishedDate,
            pages = fetchedBookData.pageCount,
            description = fetchedBookData.description,
            listPrice = null,
            category = null,
            thumbnailUrl = fetchedBookData.bookThumbnailUrl,
            registrationCount = 0,
            isSearchedNDL = false,
            ndlUrl = null,
            isSearchedGoogle = true,
            googleUrl = fetchedBookData.selfLink,
            isbn10 = fetchedBookData.isbn10,
            isbn13 = fetchedBookData.isbn13,
        )
    }

    override fun insert(book: BookEntity) {
        bookMapper.insert(book)
    }

    @Suppress("TooGenericExceptionCaught", "MaxLineLength")
    private fun convertToLocalDate(publishedDateStr: String): LocalDate? {
        if (publishedDateStr.isBlank()) return null

        return try {
            when {
                // publishedDateが完全な日付ならそのままLocalDateにパース
                publishedDateStr.length == PublishDateLength.FULL -> LocalDate.parse(publishedDateStr)

                // publishedDateが"yyyy-MM"の形式ならあとに"-01"を付加してLocalDateにパース
                publishedDateStr.length == PublishDateLength.NON_DAY -> LocalDate.parse("$publishedDateStr-01")

                // publishedDateが"yyyy"の形式ならあとに"-01-01"を付加してLocalDateにパース
                publishedDateStr.length == PublishDateLength.NON_MONTH_DAY -> LocalDate.parse("$publishedDateStr-01-01")

                else -> null
            }
        } catch (e: Exception) {
            logger.error("LocalDateへの変換でエラーが発生しました。: ${e.message}")
            null
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BookRepositoryImpl::class.java)
    }
}
