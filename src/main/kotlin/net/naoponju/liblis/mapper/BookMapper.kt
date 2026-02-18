package net.naoponju.liblis.mapper

import net.naoponju.liblis.config.UUIDTypeHandler
import net.naoponju.liblis.entity.BookEntity
import org.apache.ibatis.annotations.Delete
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Result
import org.apache.ibatis.annotations.ResultMap
import org.apache.ibatis.annotations.Results
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import java.util.UUID

@Mapper
interface BookMapper {

    // ISBNから検索
    @Select("""
        SELECT
            id
            , title
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn
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
        WHERE isbn = #{isbn}
    """)
    @Results(id =  "bookResult", value = [
        Result(id = true, column = "id", property = "id", typeHandler = UUIDTypeHandler::class),
        Result(column = "title", property = "title"),
        Result(column = "author", property = "author"),
        Result(column = "publisher", property = "publisher"),
        Result(column = "publish_date", property = "publishDate"),
        Result(column = "pages", property = "pages"),
        Result(column = "description", property = "description"),
        Result(column = "isbn", property = "isbn"),
        Result(column = "list_price", property = "listPrice"),
        Result(column = "category", property = "category"),
        Result(column = "thumbnail_url", property = "thumbnailUrl"),
        Result(column = "registration_count", property = "registrationCount"),
        Result(column = "is_searched_ndl", property = "isSearchedNDL"),
        Result(column = "ndl_url", property = "ndlUrl"),
        Result(column = "is_searched_google", property = "isSearchedGoogle"),
        Result(column = "google_url", property = "googleUrl"),
        Result(column = "created_at", property = "createdAt"),
        Result(column = "updated_at", property = "updatedAt")
    ])
    fun findByISBN(isbn: String): BookEntity?

    // IDから検索
    @Select("""
        SELECT
            id
            , title
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn
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
    """)
    @ResultMap("bookResult")
    fun findById(id: UUID): BookEntity?

    // タイトルでLIKE検索
    @Select("""
        SELECT
            id
            , title
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn
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
    """)
    @ResultMap("bookResult")
    fun findBookByTitle(title: String): List<BookEntity>

    // 著者でLIKE検索
    @Select("""
        SELECT
            id
            , title
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn
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
    """)
    @ResultMap("bookResult")
    fun findBookByAuthor(author: String): List<BookEntity>

    // すべての本をタイトル昇順で検索
    @Select("""
        SELECT
            id
            , title
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn
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
    """)
    @ResultMap("bookResult")
    fun fetchAllBooksOrderByTitle(): List<BookEntity>

    // すべての本を追加された日時が新しい順で取得
    @Select("""
        SELECT
            id
            , title
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn
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
    """)
    @ResultMap("bookResult")
    fun fetchAllBooksOrderByCreatedAtDesc(): List<BookEntity>

    // すべての本を追加された日時が古い順で取得
    @Select("""
        SELECT
            id
            , title
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn
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
    """)
    @ResultMap("bookResult")
    fun fetchAllBooksOrderByCreatedAtAsc(): List<BookEntity>

    // 新しく追加されたlimit冊を検索
    @Select("""
        SELECT
            id
            , title
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn
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
    """)
    @ResultMap("bookResult")
    fun fetchRecentBooks(limit: Int): List<BookEntity>

    // 登録件数をカウント
    @Select("SELECT COUNT(*) FROM books")
    fun countAllBooks(): Int

    @Select("""
        SELECT b.* FROM books b
        INNER JOIN user_books ub ON b.id = ub.book_id
        WHERE ub.user_id = #{userId, jdbcType=OTHER}
        AND ub.status = 'OWNED'
        ORDER BY ub.created_at DESC
    """)
    @ResultMap("bookResult")
    fun fetchUserBooks(userId: UUID): List<BookEntity>

    @Insert("""
        INSERT INTO books (
            id
            , title
            , author
            , publisher
            , publish_date
            , pages
            , description
            , isbn
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
            , #{author}
            , #{publisher}
            , #{publisherDate}
            , #{pages}
            , #{description}
            , #{isbn}
            , #{listPrice}
            , #{category}
            , #{thumnbnailUrl}
            , #{registrationCount}
            , #{isSearchedNDL}
            , #{ndlUrl}
            , #{isSearchedGoogle}
            , #{googleUrl}
            , CURRENT_TIMESTAMP
            , CURRENT_TIMESTAMP
        );
    """)
    fun insert(bookInfo: BookEntity)

    @Update("""
        UPDATE books SET
            title = #{title},
            author = #{author},
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
    """)
    fun update(bookInfo: BookEntity)

    @Delete("""
        DELETE FROM books WHERE id = #{id};
    """)
    fun delete(id: UUID)
}