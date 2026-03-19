-- ================================================================
-- V001__initial_schema.sql
-- 初回リリース用スキーマ定義（開発フェーズの V001〜V006 を統合）
-- ================================================================

-- ----------------------------------------------------------------
-- users テーブル
-- ----------------------------------------------------------------
CREATE TABLE users (
  id                UUID         NOT NULL
, display_name      VARCHAR(100) NOT NULL
, mail_address      VARCHAR(255) NOT NULL
, password_hash     VARCHAR(255) NOT NULL
, role              VARCHAR(10)  NOT NULL DEFAULT 'USER'
, google_auth       VARCHAR(255)          DEFAULT NULL
, apple_auth        VARCHAR(255)          DEFAULT NULL
, github_auth       VARCHAR(255)          DEFAULT NULL
, profile_image_url TEXT                  DEFAULT NULL
, is_deleted        BOOLEAN      NOT NULL DEFAULT FALSE
, created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
, updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
, CONSTRAINT PK_USERS PRIMARY KEY (id)
);

-- 有効ユーザー間のみ重複禁止（論理削除対応）
CREATE UNIQUE INDEX USERS_IX1
  ON users (mail_address) WHERE (is_deleted IS FALSE);
CREATE UNIQUE INDEX IDX_USERS_GOOGLE_AUTH
  ON users (google_auth)  WHERE (google_auth IS NOT NULL);
CREATE UNIQUE INDEX IDX_USERS_APPLE_AUTH
  ON users (apple_auth)   WHERE (apple_auth  IS NOT NULL);
CREATE UNIQUE INDEX IDX_USERS_GITHUB_AUTH
  ON users (github_auth)  WHERE (github_auth IS NOT NULL);

-- ----------------------------------------------------------------
-- books テーブル（書籍マスター）
-- ----------------------------------------------------------------
CREATE TABLE books (
  id                    UUID            NOT NULL
, title                 VARCHAR(255)    NOT NULL
, title_kana            VARCHAR(255)
, sub_title             VARCHAR(255)
, sub_title_kana        VARCHAR(255)
, author                TEXT[]          DEFAULT '{}'
, isbn10                VARCHAR(10)     DEFAULT NULL
, isbn13                VARCHAR(13)     DEFAULT NULL
, list_price            INTEGER         DEFAULT NULL
, category              VARCHAR(100)    DEFAULT NULL
, registration_count    INTEGER         NOT NULL DEFAULT 0
, publisher             VARCHAR(255)    DEFAULT NULL
, book_size             INTEGER         DEFAULT NULL
, publish_date          DATE            DEFAULT NULL
, pages                 INTEGER         DEFAULT NULL
, description           TEXT            DEFAULT NULL
, small_thumbnail_url   TEXT            DEFAULT NULL
, thumbnail_url         TEXT            DEFAULT NULL
, large_thumbnail_url   TEXT            DEFAULT NULL
, is_searched_ndl       BOOLEAN         NOT NULL DEFAULT FALSE
, ndl_url               TEXT            DEFAULT NULL
, is_searched_google    BOOLEAN         NOT NULL DEFAULT FALSE
, google_url            TEXT            DEFAULT NULL
, is_searched_rakuten   BOOLEAN         NOT NULL DEFAULT FALSE
, rakuten_item_url      TEXT            DEFAULT NULL
, rakuten_affiliate_url TEXT            DEFAULT NULL
, created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
, updated_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
, CONSTRAINT BOOKS_PK PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IDX_BOOKS_ISBN10
    ON books (isbn10) WHERE (isbn10 IS NOT NULL);
CREATE UNIQUE INDEX IDX_BOOKS_ISBN13
    ON books (isbn13) WHERE (isbn13 IS NOT NULL);


COMMENT ON COLUMN books.author IS '著者名（複数名対応のため配列型）';
COMMENT ON COLUMN books.rakuten_item_url IS '楽天ブックス商品URL。利用規約上リンク表示が必須';

-- ----------------------------------------------------------------
-- user_books テーブル（蔵書管理）
-- ----------------------------------------------------------------
CREATE TABLE user_books (
  id             UUID        NOT NULL
, user_id        UUID        NOT NULL
, book_id        UUID        NOT NULL
, status         VARCHAR(20) NOT NULL DEFAULT 'OWNED'
, purchase_price INTEGER              DEFAULT NULL
, purchase_date  DATE                 DEFAULT NULL
, is_deleted     BOOLEAN     NOT NULL DEFAULT FALSE
, created_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
, updated_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
, CONSTRAINT USER_BOOKS_PK      PRIMARY KEY (id)
, CONSTRAINT USER_BOOKS_FK_USER FOREIGN KEY (user_id) REFERENCES users(id)
, CONSTRAINT USER_BOOKS_FK_BOOK FOREIGN KEY (book_id) REFERENCES books(id)
);

-- 有効レコード内の user_id × book_id 重複禁止
CREATE UNIQUE INDEX idx_user_books_unique_entry
  ON user_books (user_id, book_id) WHERE (is_deleted IS FALSE);