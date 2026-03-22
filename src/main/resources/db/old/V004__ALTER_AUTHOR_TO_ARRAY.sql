-- authorカラムを単一文字列からテキスト配列型に変更
ALTER TABLE books
  ALTER COLUMN author TYPE TEXT[]
  USING (
    CASE
      WHEN author IS NULL OR author = '' THEN '{}'::TEXT[]
      ELSE ARRAY[author]
    END
  );

-- ついでにデフォルト値を空の配列に設定しておくと、Kotlin側で扱いやすくなります
ALTER TABLE books
  ALTER COLUMN author SET DEFAULT '{}'::TEXT[];

-- コメント：配列型に変更したことを記録
COMMENT ON COLUMN books.author IS '著者名（複数名対応のため配列型に変更）';