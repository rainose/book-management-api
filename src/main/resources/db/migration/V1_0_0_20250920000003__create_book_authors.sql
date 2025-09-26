CREATE TABLE t_book_authors (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES m_books(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES m_authors(id) ON DELETE CASCADE
);

-- Add table and column comments
COMMENT ON TABLE t_book_authors IS '書籍著者関連テーブル（多対多関係）';
COMMENT ON COLUMN t_book_authors.book_id IS '書籍ID（m_booksテーブル参照）';
COMMENT ON COLUMN t_book_authors.author_id IS '著者ID（m_authorsテーブル参照）';