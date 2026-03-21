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
@Suppress("TooManyFunctions")
class BookRepositoryImpl(
    private val bookMapper: BookMapper,
    private val googleBooksApiClient: GoogleBooksApiClient,
) : BookRepository {
    override fun findBookByISBN(isbn: String): BookEntity? {
        return bookMapper.findByISBN(isbn)
    }

    override fun findBookListByBookIdList(bookIds: List<UUID>): List<BookEntity> {
        return bookMapper.fetchBookList(bookIds)
    }

    override fun fetchAllBooks(): List<BookEntity> {
        return bookMapper.fetchAllBooksOrderByTitle() ?: emptyList()
    }

    /**
     * Retrieve books owned by the specified user whose IDs are in the given list.
     *
     * @param userId The user's UUID to filter ownership.
     * @param bookIds The list of book UUIDs to match.
     * @return A list of matching `BookEntity` objects that the user has, or `null` if no matching records are found.
     */
    override fun fetchUserHavingBookIdsInBookIdList(
        userId: UUID,
        bookIds: List<UUID>,
    ): List<BookEntity>? {
        return bookMapper.fetchUserHavingBookIdsInBookIdList(userId, bookIds)
    }

    /**
     * Fetches a page of books owned by the specified user.
     *
     * @param userId The UUID of the user whose books to fetch.
     * @param offset Zero-based index of the first book to return.
     * @param limit Maximum number of books to return.
     * @return A list of BookEntity in the requested page for the user; an empty list if no books are found.
     */
    override fun fetchUserHavingBooksPaged(
        userId: UUID,
        offset: Int,
        limit: Int,
    ): List<BookEntity> {
        return bookMapper.fetchUserBooksPaged(userId, offset, limit) ?: emptyList()
    }

    /**
     * Fetches books whose title matches the given string.
     *
     * @param title The title to search for.
     * @return A list of BookEntity matching the given title, or an empty list if none are found.
     */
    override fun findBookByTitle(title: String): List<BookEntity> {
        return bookMapper.findBookByTitle(title) ?: emptyList()
    }

    override fun findBookByAuthor(author: String): List<BookEntity> {
        return bookMapper.findBookByAuthor(author) ?: emptyList()
    }

    override fun fetchRecentBooks(limit: Int): List<BookEntity> {
        return bookMapper.fetchRecentBooks(limit) ?: emptyList()
    }

    /**
     * Fetches a page of books using the given offset and limit.
     *
     * @param offset Zero-based index of the first book to include.
     * @param limit Maximum number of books to return.
     * @return A list of BookEntity objects containing up to `limit` books starting from `offset`; may be empty.
     */
    override fun findAllPaged(
        offset: Int,
        limit: Int,
    ): List<BookEntity> {
        return bookMapper.findAllPaged(offset, limit)
    }

    /**
     * Retrieve a book entity by its UUID.
     *
     * @param id The UUID of the book to retrieve.
     * @return The matching BookEntity if found, `null` otherwise.
     */
    override fun findById(id: UUID): BookEntity? {
        return bookMapper.findById(id)
    }

    /**
     * Creates a BookEntity populated with metadata fetched from Google Books for the given ISBN.
     *
     * The returned entity contains mapped fields from the Google response (title, authors, publisher,
     * publishDate if convertible, page count, description, thumbnail URL, ISBN-10/13, and googleUrl).
     * External-service flags and service-specific fields are set to indicate a Google-origin record
     * (`isSearchedGoogle = true`) while Rakuten/NDL-related fields and listPrice/category are left null
     * or set to defaults.
     *
     * @param isbn The ISBN used to fetch metadata from Google Books.
     * @return A BookEntity initialized with the fetched Google Books metadata and repository-default values.
     */
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
            smallThumbnailUrl = null,
            thumbnailUrl = fetchedBookData.bookThumbnailUrl,
            largeThumbnailUrl = null,
            registrationCount = 0,
            isSearchedNDL = false,
            ndlUrl = null,
            isSearchedGoogle = true,
            googleUrl = fetchedBookData.selfLink,
            isSearchedRakuten = false,
            rakutenItemUrl = null,
            rakutenAffiliateUrl = null,
            isbn10 = fetchedBookData.isbn10,
            isbn13 = fetchedBookData.isbn13,
        )
    }

    override fun countAllBooks(): Int {
        return bookMapper.countAllBooks()
    }

    override fun insert(book: BookEntity) {
        bookMapper.insert(book)
    }

    /**
     * Converts a published-date string into a LocalDate, supporting year-only and year-month formats.
     *
     * Accepts:
     * - "yyyy-MM-dd" (returns that date),
     * - "yyyy-MM" (assumes day = 01),
     * - "yyyy" (assumes month = 01 and day = 01).
     *
     * @param publishedDateStr The published date string to convert.
     * @return The corresponding LocalDate, or `null` if the input is blank, has an unsupported format, or parsing fails (an error is logged).
     */
    @Suppress("TooGenericExceptionCaught", "MaxLineLength")
    private fun convertToLocalDate(publishedDateStr: String): LocalDate? {
        if (publishedDateStr.isBlank()) return null

        return try {
            when {
                publishedDateStr.length == PublishDateLength.FULL ->
                    LocalDate.parse(publishedDateStr)
                publishedDateStr.length == PublishDateLength.NON_DAY ->
                    LocalDate.parse("$publishedDateStr-01")
                publishedDateStr.length == PublishDateLength.NON_MONTH_DAY ->
                    LocalDate.parse("$publishedDateStr-01-01")
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
