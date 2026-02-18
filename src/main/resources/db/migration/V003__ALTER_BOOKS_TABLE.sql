ALTER TABLE books ADD publisher VARCHAR(255);
ALTER TABLE books ADD publish_date DATE;
ALTER TABLE books ADD pages INTEGER;
ALTER TABLE books ADD description TEXT;
ALTER TABLE books ADD thumbnail_url TEXT;
ALTER TABLE books ADD is_searched_ndl BOOLEAN DEFAULT FALSE;
ALTER TABLE books ADD ndl_url TEXT;
ALTER TABLE books ADD is_searched_google BOOLEAN DEFAULT FALSE;
ALTER TABLE books ADD google_url TEXT;
