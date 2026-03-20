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
@Suppress("TooManyFunctions")
interface BookMapper {
    // ISBNから検索
    @Select(
        """
        SELECT
            id
            , title
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
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
            Result(column = "author", property = "author", typeHandler = StringListTypeHandler::class),
            Result(column = "publisher", property = "publisher"),
            Result(column = "publish_date", property = "publishDate"),
            Result(column = "pages", property = "pages"),
            Result(column = "description", property = "description"),
            Result(column = "isbn10", property = "isbn10"),
            Result(column = "isbn13", property = "isbn13"),
            Result(column = "list_price", property = "listPrice"),
            Result(column = "category", property = "category"),
            Result(column = "thumbnail_url", property = "thumbnailUrl"),
            Result(column = "registration_count", property = "registrationCount"),
            Result(column = "is_searched_ndl", property = "isSearchedNDL"),
            Result(column = "ndl_url", property = "ndlUrl"),
            Result(column = "is_searched_google", property = "isSearchedGoogle"),
            Result(column = "google_url", property = "googleUrl"),
            Result(column = "created_at", property = "createdAt"),
            Result(column = "updated_at", property = "updatedAt"),
        ],
    )
    fun findByISBN(isbn: String): BookEntity?

    // 複数のBookIDのBookEntityを取得
    @Select(
        """
            <script>
                SELECT
                    id
                    , title
                    , author
                    , publisher
                    , publish_date
                    , pages
                    , description
                    , isbn10
                    , isbn13
                    , list_price
                    , category
                    , thumbnail_url
                    , registration_count
                    , is_searched_ndl
                    , ndl_url
                    , is_searched_google
                    , google_url
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
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
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
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
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
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
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
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
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
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
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
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
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
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
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
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
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
                , b.author
                , b.publisher
                , b.publish_date
                , b.pages
                , b.description
                , b.isbn10
                , b.isbn13
                , b.list_price
                , b.category
                , b.thumbnail_url
                , b.registration_count
                , b.is_searched_ndl
                , b.ndl_url
                , b.is_searched_google
                , b.google_url
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
                , b.author
                , b.publisher
                , b.publish_date
                , b.pages
                , b.description
                , b.isbn10
                , b.isbn13
                , b.list_price
                , b.category
                , b.thumbnail_url
                , b.registration_count
                , b.is_searched_ndl
                , b.ndl_url
                , b.is_searched_google
                , b.google_url
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

    @Insert(
        """
        INSERT INTO books (
            id
            , title
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn10
            , isbn13
            , list_price
            , category
            , thumbnail_url
            , registration_count
            , is_searched_ndl
            , ndl_url
            , is_searched_google
            , google_url
            , created_at
            , updated_at
        ) VALUES (
            #{id, jdbcType=OTHER}
            , #{title}
            , #{author, typeHandler=net.naoponju.liblis.common.config.StringListTypeHandler}
            , #{publisher}
            , #{publishDate}
            , #{pages}
            , #{description}
            , #{isbn10}
            , #{isbn13}
            , #{listPrice}
            , #{category}
            , #{thumbnailUrl}
            , #{registrationCount}
            , #{isSearchedNDL}
            , #{ndlUrl}
            , #{isSearchedGoogle}
            , #{googleUrl}
            , CURRENT_TIMESTAMP
            , CURRENT_TIMESTAMP
        );
    """,
    )
    fun insert(bookInfo: BookEntity)

    @Update(
        """
        UPDATE books SET
            title = #{title},
            author = #{author, typeHandler=net.naoponju.liblis.common.config.StringListTypeHandler},
            publisher = #{publisher},
            publish_date = #{publishDate},
            pages = #{pages},
            description = #{description},
            list_price = #{listPrice},
            category = #{category},
            thumbnail_url = #{thumbnailUrl},
            registration_count = #{registrationCount},
            is_searched_ndl = #{isSearchedNDL},
            ndl_url = #{ndlUrl},
            is_searched_google = #{isSearchedGoogle},
            google_url = #{googleUrl},
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
