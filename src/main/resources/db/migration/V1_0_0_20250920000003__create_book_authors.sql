CREATE TABLE t_book_authors (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES m_books(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES m_authors(id) ON DELETE CASCADE
);