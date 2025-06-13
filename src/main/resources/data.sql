-- Sample borrowers
INSERT INTO borrowers (name, email) VALUES 
    ('John Doe', 'john.doe@email.com'),
    ('Jane Smith', 'jane.smith@email.com'),
    ('Alice Johnson', 'alice.johnson@email.com');

-- Sample books
INSERT INTO books (title, author, isbn, available, borrowed_by) VALUES 
    ('The Great Gatsby', 'F. Scott Fitzgerald', '9780743273565', true, null),
    ('To Kill a Mockingbird', 'Harper Lee', '9780061120084', true, null),
    ('1984', 'George Orwell', '9780451524935', false, 1),
    ('Pride and Prejudice', 'Jane Austen', '9780141439518', true, null),
    ('The Catcher in the Rye', 'J.D. Salinger', '9780316769174', false, 2);