CREATE TABLE rakuten_books_genre (
    books_genre_id          VARCHAR(12)  NOT NULL
  , genre_level             SMALLINT     NOT NULL
  , parent_books_genre_id   VARCHAR(12)
  , books_genre_id_1        VARCHAR(3)
  , books_genre_id_2        VARCHAR(3)
  , books_genre_id_3        VARCHAR(3)
  , books_genre_id_4        VARCHAR(3)
  , books_genre_name_1      VARCHAR(50)
  , books_genre_name_2      VARCHAR(50)
  , books_genre_name_3      VARCHAR(50)
  , books_genre_name_4      VARCHAR(50)
  , created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
  , updated_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
  , CONSTRAINT pk_rakuten_books_genre PRIMARY KEY (books_genre_id)
  , CONSTRAINT fk_rakuten_books_genre_parent
      FOREIGN KEY (parent_books_genre_id)
      REFERENCES rakuten_books_genre (books_genre_id)
);

CREATE INDEX idx_rakuten_books_genre_level  ON rakuten_books_genre (genre_level);
CREATE INDEX idx_rakuten_books_genre_parent ON rakuten_books_genre (parent_books_genre_id);
CREATE INDEX idx_rakuten_books_genre_lv1    ON rakuten_books_genre (books_genre_id_1);

CREATE VIEW v_rakuten_books_genre AS
SELECT
    books_genre_id
  , genre_level
  , parent_books_genre_id
  , CASE genre_level
      WHEN 1 THEN books_genre_name_1
      WHEN 2 THEN books_genre_name_2
      WHEN 3 THEN books_genre_name_3
      WHEN 4 THEN books_genre_name_4
    END                              AS books_genre_name
  , CONCAT_WS(' > ',
      books_genre_name_1,
      books_genre_name_2,
      books_genre_name_3,
      books_genre_name_4
    )                                AS books_genre_path
  , books_genre_id_1, books_genre_id_2, books_genre_id_3, books_genre_id_4
  , books_genre_name_1, books_genre_name_2, books_genre_name_3, books_genre_name_4
  , created_at, updated_at
FROM rakuten_books_genre;