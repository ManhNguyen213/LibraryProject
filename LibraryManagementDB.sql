DROP TABLE IF EXISTS Invoice_Details;
DROP TABLE IF EXISTS Invoices;
DROP TABLE IF EXISTS Books;
DROP TABLE IF EXISTS Members;
DROP TABLE IF EXISTS Accounts;
DROP TABLE IF EXISTS Rank_Policies;

CREATE TABLE Accounts (
    account_id VARCHAR(20) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(10) NOT NULL CHECK (role IN ('manager', 'member')),
    is_active BIT DEFAULT 1
);

CREATE TABLE Rank_Policies (
    rank VARCHAR(20) PRIMARY KEY,
    discount_percent INT CHECK (discount_percent BETWEEN 0 AND 100)
);

CREATE TABLE Members (
    account_id VARCHAR(20) PRIMARY KEY,
    full_name VARCHAR(100),
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE,
    address VARCHAR(255),
    rank VARCHAR(20) DEFAULT 'Bronze',
    FOREIGN KEY (account_id) REFERENCES Accounts(account_id) ON DELETE CASCADE,
	FOREIGN KEY (rank) REFERENCES Rank_Policies(rank)
);

CREATE TABLE Books (
    book_id VARCHAR(20) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    author VARCHAR(100),
    genre VARCHAR(50),
    quantity INT NOT NULL,
    price DECIMAL(10, 2)
);

CREATE TABLE Invoices (
    invoice_id VARCHAR(20) PRIMARY KEY,
    member_id VARCHAR(20),
    employee_id VARCHAR(20) NOT NULL,
    date_created DATE NOT NULL,
    total_price DECIMAL(10,2),
    discount_applied INT DEFAULT 0,
    FOREIGN KEY (member_id) REFERENCES Members(account_id),
    FOREIGN KEY (employee_id) REFERENCES Accounts(account_id)
);

CREATE TABLE Invoice_Details (
    invoice_id VARCHAR(20),
    book_id VARCHAR(20),
    quantity INT NOT NULL CHECK (quantity > 0),
    price_each DECIMAL(10,2),
    PRIMARY KEY (invoice_id, book_id),
    FOREIGN KEY (invoice_id) REFERENCES Invoices(invoice_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES Books(book_id)
);

INSERT INTO Rank_Policies (rank, discount_percent) VALUES
('Bronze', 0), ('Silver', 5), ('Gold', 10), ('Platinum', 15), ('Emerald', 20), ('Diamond', 25);


INSERT INTO Accounts (account_id, username, password_hash, role, is_active) VALUES 
('A01', 'admin', '123', 'manager', 1),
('M01', 'member', '123', 'member', 1),
('M02', 'member', '123', 'member', 1),
('M03', 'member', '123', 'member', 1);


INSERT INTO Members (account_id, full_name, phone, email, address)
VALUES 
('M01', 'Nguyen Duc Manh', '0123456789', 'manh@example.com', 'Ha Noi'),
('M02', 'Nguyen Sy Loc', '03232323232', 'loc@example.com', 'Thanh Hoa'),
('M03', 'Le Dinh Minh', '0111111111', 'minh@example.com', 'Hai Duong');

INSERT INTO Invoices (invoice_id, member_id, employee_id, date_created)
VALUES 
('IV01', 'M01', 'A01', '2025-06-01'),
('IV02', 'M02', 'A01', '2025-06-02'),
('IV03', 'M03', 'A01', '2025-06-03');

INSERT INTO books (book_id, title, author, genre, quantity, price) VALUES
	('B01', 'Effective Java', 'Joshua Bloch', 'Science', 5, 15.62),
	('B02', 'Design Patterns', 'Erich Gamma', 'Science', 7, 5.78),
	('B03', 'Clean Code', 'Robert C. Martin', 'Science', 10, 21.16);

INSERT INTO Invoice_Details (invoice_id, book_id, quantity)
VALUES 
('IV01', 'B02', '1'),
('IV01', 'B01', '2'),
('IV02', 'B02', '3'),
('IV03', 'B03', '7');

SELECT * FROM books;
SELECT * FROM Members;
SELECT * FROM Accounts;

--------------------------------TRIGGER------------------------------------------

CREATE TRIGGER trg_set_price_each
ON Invoice_Details
INSTEAD OF INSERT
AS
BEGIN
    INSERT INTO Invoice_Details (invoice_id, book_id, quantity, price_each)
    SELECT 
        i.invoice_id,
        i.book_id,
        i.quantity,
        b.price  -- lấy giá sách từ bảng Books
    FROM 
        inserted i
    JOIN 
        Books b ON i.book_id = b.book_id;
END;


CREATE TRIGGER trg_CalculateInvoiceTotal
ON Invoices
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @invoice_id VARCHAR(20);
    DECLARE @member_id VARCHAR(20);
    DECLARE @discount INT = 0;
    DECLARE @rank VARCHAR(20);
    DECLARE @total DECIMAL(10,2) = 0;

    DECLARE insert_cursor CURSOR FOR
        SELECT invoice_id, member_id FROM inserted;

    OPEN insert_cursor;
    FETCH NEXT FROM insert_cursor INTO @invoice_id, @member_id;

    WHILE @@FETCH_STATUS = 0
    BEGIN
        -- Lấy rank và discount nếu là member
        IF @member_id IS NOT NULL
        BEGIN
            SELECT @rank = M.rank
            FROM Members M
            WHERE M.account_id = @member_id;

            SELECT @discount = discount_percent
            FROM Rank_Policies
            WHERE rank = @rank;
        END

        SELECT @total = SUM(quantity * price_each)
        FROM Invoice_Details
        WHERE invoice_id = @invoice_id;

        IF @total IS NULL
            SET @total = 0;

        UPDATE Invoices
        SET total_price = @total,
            discount_applied = @discount
        WHERE invoice_id = @invoice_id;

        FETCH NEXT FROM insert_cursor INTO @invoice_id, @member_id;
    END

    CLOSE insert_cursor;
    DEALLOCATE insert_cursor;
END;

CREATE OR ALTER TRIGGER trg_UpdateTotalPrice
ON Invoice_Details
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @affectedInvoices TABLE (invoice_id VARCHAR(20) PRIMARY KEY);

    INSERT INTO @affectedInvoices(invoice_id)
    SELECT DISTINCT invoice_id FROM inserted
    WHERE invoice_id IS NOT NULL

    UNION

    SELECT DISTINCT invoice_id FROM deleted
    WHERE invoice_id IS NOT NULL;

    UPDATE i
    SET total_price = ISNULL(sub.total, 0)
    FROM Invoices i
    INNER JOIN @affectedInvoices ai ON i.invoice_id = ai.invoice_id
    CROSS APPLY (
        SELECT SUM(quantity * price_each) AS total
        FROM Invoice_Details
        WHERE invoice_id = i.invoice_id
    ) sub;

    UPDATE i
    SET discount_applied = rp.discount_percent
    FROM Invoices i
    LEFT JOIN Members m ON i.member_id = m.account_id
    LEFT JOIN Rank_Policies rp ON m.rank = rp.rank
    INNER JOIN @affectedInvoices ai ON i.invoice_id = ai.invoice_id;
END;