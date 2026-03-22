package net.naoponju.liblis.infra.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

@Mapper
interface RakutenBooksGenreMapper {
    @Select(
        """
        SELECT books_genre_path
        FROM v_rakuten_books_genre
        WHERE books_genre_id = #{booksGenreId}
        LIMIT 1
    """,
    )
    fun findGenrePathById(booksGenreId: String): String?

    @Select(
        """
            <script>
                SELECT books_genre_id,
                       COALESCE(books_genre_name_3, books_genre_name_2) AS display_genre_name
                FROM v_rakuten_books_genre
                WHERE books_genre_id IN
                <foreach item="id" collection="ids" open="(" separator="," close=")">
                    #{id}
                </foreach>
            </script>
        """,
    )
    fun findGenreNamesByIds(
        @Param("ids") ids: List<String>,
    ): List<Map<String, String>>
}
