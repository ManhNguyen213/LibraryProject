package service;

import model.Book;
import repository.BookRepository;

import java.util.List;
import java.util.Optional;

public class BookService {
    private final BookRepository bookRepository;

    public BookService() {
        this.bookRepository = new BookRepository();
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public boolean addBook(Book book) {
        if (bookRepository.findById(book.getBookID()).isPresent()) {
            System.err.println("Book ID already exists.");
            return false;
        }
        return bookRepository.save(book);
    }

    public boolean updateBook(Book book, String originalId) {
        if (!book.getBookID().equals(originalId)) {
            if (bookRepository.findById(book.getBookID()).isPresent()) {
                System.err.println("New Book ID already exists.");
                return false;
            }
        }
        return bookRepository.update(book, originalId);
    }

    public boolean deleteBook(String id) {
        return bookRepository.deleteById(id);
    }

    public int getTotalAvailableQuantity() {
        return bookRepository.getTotalAvailableQuantity();
    }
}
