CREATE TABLE users (
  id UUID NOT NULL
  , display_name VARCHAR(100) NOT NULL
  , mail_address VARCHAR(255) NOT NULL
  , password_hash VARCHAR(255) NOT NULL
  , role VARCHAR(10) DEFAULT 'USER' NOT NULL
  , google_auth VARCHAR(255) DEFAULT NULL
  , apple_auth VARCHAR(255) DEFAULT NULL
  , github_auth VARCHAR(255) DEFAULT NULL
  , profile_image_url TEXT DEFAULT NULL
  , is_deleted BOOLEAN DEFAULT FALSE
  , created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  , updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  , CONSTRAINT PK_USERS PRIMARY KEY (id)
);

-- 通常の一意制約の代わりに、有効なユーザー間のみで重複を禁止する
CREATE UNIQUE INDEX USERS_IX1 ON users (mail_address) WHERE (is_deleted IS FALSE);
CREATE UNIQUE INDEX IDX_USERS_GOOGLE_AUTH ON users (google_auth) WHERE (google_auth IS NOT NULL);
CREATE UNIQUE INDEX IDX_USERS_APPLE_AUTH ON users (apple_auth) WHERE (apple_auth IS NOT NULL);
CREATE UNIQUE INDEX IDX_USERS_GITHUB_AUTH ON users (github_auth) WHERE (github_auth IS NOT NULL);
