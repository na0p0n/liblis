-- ISBNカラムをISBN10とISBN13に分割するマイグレーション

-- 1. 新しいカラムを追加
ALTER TABLE liblis_dev.books
ADD COLUMN isbn10 CHARACTER VARYING(10),
ADD COLUMN isbn13 CHARACTER VARYING(13);

-- 2. (任意) 既存のISBNデータが13桁ならisbn13に、10桁ならisbn10に移送する
-- ※データがない場合はこのステップは不要です
UPDATE liblis_dev.books
SET
  isbn13 = CASE WHEN LENGTH(isbn) = 13 THEN isbn END,
  isbn10 = CASE WHEN LENGTH(isbn) = 10 THEN isbn END;

-- 3. 旧isbnカラムを削除
ALTER TABLE liblis_dev.books
DROP COLUMN isbn;
