package net.naoponju.liblis.infra.mapper

import net.naoponju.liblis.common.config.StringListTypeHandler
import net.naoponju.liblis.common.config.UUIDTypeHandler
import net.naoponju.liblis.domain.entity.BookEntity
import org.apache.ibatis.annotations.Delete
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Result
import org.apache.ibatis.annotations.ResultMap
import org.apache.ibatis.annotations.Results
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import java.util.UUID

@Mapper
@Suppress("TooManyFunctions", "LargeClass")
interface BookMapper {
    // ISBNから検索
    /**
     * Finds a book by its ISBN, matching against either the ISBN-10 or ISBN-13 columns.
     *
     * @param isbn The ISBN to search for; may be an ISBN-10 or ISBN-13.
     * @return `BookEntity` matching the ISBN, or `null` if no matching record is found.
     */
    @Select(
        """
        SELECT
            id
            , title
            , title_kana
            , sub_title
            , sub_title_kana
            , author
            , publisher
            , book_size
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , small_thumbnail_url
            , thumbnail_url
            , large_thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
            , is_searched_rakuten
            , rakuten_item_url
            , rakuten_affiliate_url
            , created_at
            , updated_at
        FROM books
        WHERE isbn10 = #{isbn} OR isbn13 = #{isbn};
    """,
    )
    @Results(
        id = "bookResult",
        value = [
            Result(id = true, column = "id", property = "id", typeHandler = UUIDTypeHandler::class),
            Result(column = "title", property = "title"),
            Result(column = "title_kana", property = "titleKana"),
            Result(column = "sub_title", property = "subTitle"),
            Result(column = "sub_title_kana", property = "subTitleKana"),
            Result(column = "author", property = "author", typeHandler = StringListTypeHandler::class),
            Result(column = "publisher", property = "publisher"),
            Result(column = "book_size", property = "bookSize"),
            Result(column = "publish_date", property = "publishDate"),
            Result(column = "pages", property = "pages"),
            Result(column = "description", property = "description"),
            Result(column = "isbn10", property = "isbn10"),
            Result(column = "isbn13", property = "isbn13"),
            Result(column = "list_price", property = "listPrice"),
            Result(column = "category", property = "category"),
            Result(column = "small_thumbnail_url", property = "smallThumbnailUrl"),
            Result(column = "thumbnail_url", property = "thumbnailUrl"),
            Result(column = "large_thumbnail_url", property = "largeThumbnailUrl"),
            Result(column = "registration_count", property = "registrationCount"),
            Result(column = "is_searched_ndl", property = "isSearchedNDL"),
            Result(column = "ndl_url", property = "ndlUrl"),
            Result(column = "is_searched_google", property = "isSearchedGoogle"),
            Result(column = "google_url", property = "googleUrl"),
            Result(column = "is_searched_rakuten", property = "isSearchedRakuten"),
            Result(column = "rakuten_item_url", property = "rakutenItemUrl"),
            Result(column = "rakuten_affiliate_url", property = "rakutenAffiliateUrl"),
            Result(column = "created_at", property = "createdAt"),
            Result(column = "updated_at", property = "updatedAt"),
        ],
    )
    fun findByISBN(isbn: String): BookEntity?

    /**
     * Retrieve book entities for the given book IDs.
     *
     * @param bookIds The list of book UUIDs to fetch.
     * @return A list of BookEntity matching the provided IDs; empty list if none match.
     */
    @Select(
        """
            <script>
                SELECT
                    id
                    , title
                    , title_kana
                    , sub_title
                    , sub_title_kana
                    , author
                    , publisher
                    , book_size
                    , publish_date
                    , pages
                    , description
                    , isbn10
                    , isbn13
                    , list_price
                    , category
                    , small_thumbnail_url
                    , thumbnail_url
                    , large_thumbnail_url
                    , registration_count
                    , is_searched_ndl
                    , ndl_url
                    , is_searched_google
                    , google_url
                    , is_searched_rakuten
                    , rakuten_item_url
                    , rakuten_affiliate_url
                    , created_at
                    , updated_at
                FROM books
                WHERE id IN
                <foreach item="id" collection="bookIds" open="(" separator="," close=")">
                    #{id}
                </foreach>
            </script>
        """,
    )
    @ResultMap("bookResult")
    fun fetchBookList(bookIds: List<UUID>): List<BookEntity>

    // IDから検索
    @Select(
        """
        SELECT
            id
            , title
            , title_kana
            , sub_title
            , sub_title_kana
            , author
            , publisher
            , book_size
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , small_thumbnail_url
            , thumbnail_url
            , large_thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
            , is_searched_rakuten
            , rakuten_item_url
            , rakuten_affiliate_url
            , created_at
            , updated_at
        FROM books
        WHERE id = #{id, jdbcType=OTHER}
    """,
    )
    @ResultMap("bookResult")
    fun findById(id: UUID): BookEntity?

    // タイトルでLIKE検索
    @Select(
        """
        SELECT
            id
            , title
            , title_kana
            , sub_title
            , sub_title_kana
            , author
            , publisher
            , book_size
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , small_thumbnail_url
            , thumbnail_url
            , large_thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
            , is_searched_rakuten
            , rakuten_item_url
            , rakuten_affiliate_url
            , created_at
            , updated_at
        FROM books
        WHERE title LIKE '%' || #{title} || '%'
    """,
    )
    @ResultMap("bookResult")
    fun findBookByTitle(title: String): List<BookEntity>?

    // 著者でLIKE検索
    @Select(
        """
        SELECT
            id
            , title
            , title_kana
            , sub_title
            , sub_title_kana
            , author
            , publisher
            , book_size
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , small_thumbnail_url
            , thumbnail_url
            , large_thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
            , is_searched_rakuten
            , rakuten_item_url
            , rakuten_affiliate_url
            , created_at
            , updated_at
        FROM books
        WHERE author LIKE '%' || #{author} || '%'
    """,
    )
    @ResultMap("bookResult")
    fun findBookByAuthor(author: String): List<BookEntity>?

    // すべての本をタイトル昇順で検索
    @Select(
        """
        SELECT
            id
            , title
            , title_kana
            , sub_title
            , sub_title_kana
            , author
            , publisher
            , book_size
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , small_thumbnail_url
            , thumbnail_url
            , large_thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
            , is_searched_rakuten
            , rakuten_item_url
            , rakuten_affiliate_url
            , created_at
            , updated_at
        FROM books
        ORDER BY title
    """,
    )
    @ResultMap("bookResult")
    fun fetchAllBooksOrderByTitle(): List<BookEntity>?

    // すべての本を追加された日時が新しい順で取得
    @Select(
        """
        SELECT
            id
            , title
            , title_kana
            , sub_title
            , sub_title_kana
            , author
            , publisher
            , book_size
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , small_thumbnail_url
            , thumbnail_url
            , large_thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
            , is_searched_rakuten
            , rakuten_item_url
            , rakuten_affiliate_url
            , created_at
            , updated_at
        FROM books
        ORDER BY created_at DESC
    """,
    )
    @ResultMap("bookResult")
    fun fetchAllBooksOrderByCreatedAtDesc(): List<BookEntity>?

    // すべての本を追加された日時が古い順で取得
    @Select(
        """
        SELECT
            id
            , title
            , title_kana
            , sub_title
            , sub_title_kana
            , author
            , publisher
            , book_size
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , small_thumbnail_url
            , thumbnail_url
            , large_thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
            , is_searched_rakuten
            , rakuten_item_url
            , rakuten_affiliate_url
            , created_at
            , updated_at
        FROM books
        ORDER BY created_at
    """,
    )
    @ResultMap("bookResult")
    fun fetchAllBooksOrderByCreatedAtAsc(): List<BookEntity>?

    // 新しく追加されたlimit冊を検索
    @Select(
        """
        SELECT
            id
            , title
            , title_kana
            , sub_title
            , sub_title_kana
            , author
            , publisher
            , book_size
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , small_thumbnail_url
            , thumbnail_url
            , large_thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
            , is_searched_rakuten
            , rakuten_item_url
            , rakuten_affiliate_url
            , created_at
            , updated_at
        FROM books
        ORDER BY created_at DESC
        LIMIT #{limit}
    """,
    )
    @ResultMap("bookResult")
    fun fetchRecentBooks(limit: Int): List<BookEntity>?

    // 本一覧取得のページャー
    @Select(
        """
        SELECT
            id
            , title
            , title_kana
            , sub_title
            , sub_title_kana
            , author
            , publisher
            , book_size
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , small_thumbnail_url
            , thumbnail_url
            , large_thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
            , is_searched_rakuten
            , rakuten_item_url
            , rakuten_affiliate_url
            , created_at
            , updated_at
        FROM books
        ORDER BY title
        LIMIT #{limit} OFFSET #{offset}
    """,
    )
    @ResultMap("bookResult")
    fun findAllPaged(
        @Param("offset") offset: Int,
        @Param("limit") limit: Int,
    ): List<BookEntity>

    // 登録件数をカウント
    @Select("SELECT COUNT(*) FROM books")
    fun countAllBooks(): Int

    @Select(
        """
        <script>
            SELECT 
                b.id
                , b.title
                , b.title_kana
                , b.sub_title
                , b.sub_title_kana
                , b.author
                , b.publisher
                , b.book_size
                , b.publish_date
                , b.pages
                , b.description
                , b.isbn10
                , b.isbn13
                , b.list_price
                , b.category
                , b.small_thumbnail_url
                , b.thumbnail_url
                , b.large_thumbnail_url
                , b.registration_count
                , b.is_searched_ndl
                , b.ndl_url
                , b.is_searched_google
                , b.google_url
                , b.is_searched_rakuten
                , b.rakuten_item_url
                , b.rakuten_affiliate_url
                , b.created_at
                , b.updated_at
            FROM books b
            INNER JOIN user_books ub ON b.id = ub.book_id
            WHERE ub.user_id = #{userId, jdbcType=OTHER}
            AND ub.status = 'OWNED'
            AND ub.is_deleted = false
            AND b.id IN
            <foreach item="id" collection="bookIds" open="(" separator="," close=")">
                #{id}
            </foreach>
            ORDER BY ub.created_at DESC
        </script>
    """,
    )
    @ResultMap("bookResult")
    fun fetchUserHavingBookIdsInBookIdList(
        userId: UUID,
        bookIds: List<UUID>,
    ): List<BookEntity>?

    @Select(
        """
            SELECT 
                b.id
                , b.title
                , b.title_kana
                , b.sub_title
                , b.sub_title_kana
                , b.author
                , b.publisher
                , b.book_size
                , b.publish_date
                , b.pages
                , b.description
                , b.isbn10
                , b.isbn13
                , b.list_price
                , b.category
                , b.small_thumbnail_url
                , b.thumbnail_url
                , b.large_thumbnail_url
                , b.registration_count
                , b.is_searched_ndl
                , b.ndl_url
                , b.is_searched_google
                , b.google_url
                , b.is_searched_rakuten
                , b.rakuten_item_url
                , b.rakuten_affiliate_url
                , b.created_at
                , b.updated_at
            FROM books b
            INNER JOIN user_books ub ON b.id = ub.book_id
            WHERE ub.user_id = #{userId, jdbcType=OTHER}
            AND ub.status = 'OWNED'
            AND ub.is_deleted = false
            ORDER BY ub.created_at DESC
            LIMIT #{limit} OFFSET #{offset}
        """,
    )
    @ResultMap("bookResult")
    fun fetchUserBooksPaged(
        userId: UUID,
        offset: Int,
        limit: Int,
    ): List<BookEntity>?

    /**
     * Inserts the provided book record into the `books` table and sets `created_at` and `updated_at` to the current timestamp.
     *
     * @param bookInfo The book entity to persist; the `author` list is stored using the configured string-list type handler. 
     */
    @Insert(
        """
        INSERT INTO books (
            id
            , title
            , title_kana
            , sub_title
            , sub_title_kana
            , author
            , publisher
            , book_size
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , small_thumbnail_url
            , thumbnail_url
            , large_thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
            , is_searched_rakuten
            , rakuten_item_url
            , rakuten_affiliate_url
            , created_at
            , updated_at
        ) VALUES (
            #{id, jdbcType=OTHER}
            , #{title}
            , #{titleKana}
            , #{subTitle}
            , #{subTitleKana}
            , #{author, typeHandler=net.naoponju.liblis.common.config.StringListTypeHandler}
            , #{publisher}
            , #{bookSize}
            , #{publishDate}
            , #{pages}
            , #{description}
            , #{isbn10}
            , #{isbn13}
            , #{listPrice}
            , #{category}
            , #{smallThumbnailUrl}
            , #{thumbnailUrl}
            , #{largeThumbnailUrl}
            , #{registrationCount}
            , #{isSearchedNDL}
            , #{ndlUrl}
            , #{isSearchedGoogle}
            , #{googleUrl}
            , #{isSearchedRakuten}
            , #{rakutenItemUrl}
            , #{rakutenAffiliateUrl}
            , CURRENT_TIMESTAMP
            , CURRENT_TIMESTAMP
        );
    """,
    )
    fun insert(bookInfo: BookEntity)

    /**
     * Updates an existing book record using values from the provided BookEntity and sets `updated_at` to the current timestamp.
     *
     * @param bookInfo BookEntity containing the `id` of the book to update and the new field values to persist.
     */
    @Update(
        """
        UPDATE books SET
            title = #{title},
            title_kana = #{titleKana},
            sub_title = #{subTitle},
            sub_title_kana = #{subTitleKana},
            author = #{author, typeHandler=net.naoponju.liblis.common.config.StringListTypeHandler},
            publisher = #{publisher},
            book_size = #{bookSize},
            publish_date = #{publishDate},
            pages = #{pages},
            description = #{description},
            list_price = #{listPrice},
            category = #{category},
            small_thumbnail_url = #{smallThumbnailUrl},
            thumbnail_url = #{thumbnailUrl},
            large_thumbnail_url = #{largeThumbnailUrl},
            registration_count = #{registrationCount},
            is_searched_ndl = #{isSearchedNDL},
            ndl_url = #{ndlUrl},
            is_searched_google = #{isSearchedGoogle},
            google_url = #{googleUrl},
            is_searched_rakuten = #{isSearchedRakuten},
            rakuten_item_url = #{rakutenItemUrl},
            rakuten_affiliate_url = #{rakutenAffiliateUrl},
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id, jdbcType=OTHER}
    """,
    )
    fun update(bookInfo: BookEntity)

    @Delete(
        """
        DELETE FROM books WHERE id = #{id};
    """,
    )
    fun delete(id: UUID)
}
