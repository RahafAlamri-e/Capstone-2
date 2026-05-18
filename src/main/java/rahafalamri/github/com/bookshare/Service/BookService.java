package rahafalamri.github.com.bookshare.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rahafalamri.github.com.bookshare.Api.ApiException;
import rahafalamri.github.com.bookshare.Model.Book;
import rahafalamri.github.com.bookshare.Model.User;
import rahafalamri.github.com.bookshare.Repository.BookRepository;
import rahafalamri.github.com.bookshare.Repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public List<Book> getAllBooks(Integer adminId) {
        userService.checkAdmin(adminId);
        return bookRepository.findAll();
    }

    public List<Book> getApprovedBooks() {
        return bookRepository.findApprovedBooks();
    }

    public List<Book> getPendingBooks(Integer adminId) {
        userService.checkAdmin(adminId);
        return bookRepository.findPendingBooks();
    }

    public List<Book> getRejectedBooks(Integer adminId) {
        userService.checkAdmin(adminId);
        return bookRepository.findRejectedBooks();
    }

    public void addBook(Integer ownerId, Book book) {

        User owner = userService.getUserById(ownerId);

        if (!owner.getRole().equals("MEMBER")) {
            throw new ApiException("Only members can add books");
        }

        if (owner.getIsBlocked()) {
            throw new ApiException("Blocked users cannot add books");
        }

        if (book.getAvailableCopies() > book.getTotalCopies()) {
            throw new ApiException("Available copies cannot be greater than total copies");
        }

        book.setOwnerId(ownerId);
        book.setStatus("PENDING");

        bookRepository.save(book);
    }

    public void updateBook(Integer ownerId, Integer bookId, Book book) {

        User owner = userService.getUserById(ownerId);
        Book oldBook = getBookById(bookId);

        if (!oldBook.getOwnerId().equals(ownerId)) {
            throw new ApiException("Only book owner can update this book");
        }

        if (owner.getIsBlocked()) {
            throw new ApiException("Blocked users cannot update books");
        }

        if (book.getAvailableCopies() > book.getTotalCopies()) {
            throw new ApiException("Available copies cannot be greater than total copies");
        }

        oldBook.setTitle(book.getTitle());
        oldBook.setCategory(book.getCategory());
        oldBook.setDescription(book.getDescription());
        oldBook.setTotalCopies(book.getTotalCopies());
        oldBook.setAvailableCopies(book.getAvailableCopies());
        oldBook.setRentalPrice(book.getRentalPrice());
        oldBook.setDepositAmount(book.getDepositAmount());
        oldBook.setStatus("PENDING");
        oldBook.setLocation(book.getLocation());

        bookRepository.save(oldBook);
    }

    public void deleteBook(Integer userId, Integer bookId) {

        User user = userService.getUserById(userId);

        Book book = getBookById(bookId);

        if (!user.getRole().equals("ADMIN") && !book.getOwnerId().equals(userId)) {
            throw new ApiException("Only admin or book owner can delete this book");
        }

        bookRepository.delete(book);
    }

    public void approveOrRejectBook(Integer adminId, Integer bookId, String status) {

        userService.checkAdmin(adminId);

        if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
            throw new ApiException("Status must be APPROVED or REJECTED");
        }

        Book book = getBookById(bookId);

        book.setStatus(status);
        bookRepository.save(book);
    }

    public List<Book> searchApprovedBooksByKeyword(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ApiException("Keyword cannot be empty");
        }

        return bookRepository.searchApprovedBooksByKeyword(keyword);
    }

    public List<Book> getBooksByOwner(Integer ownerId) {
        userService.getUserById(ownerId);
        return bookRepository.findBooksByOwnerId(ownerId);
    }

    public String getPlatformStats(Integer adminId) {

        userService.checkAdmin(adminId);

        return "Books: " + bookRepository.count()
                + " | Blocked Users: " + userRepository.countByIsBlocked(true);
    }

    public List<Book> getApprovedBooksByLocation(String location) {
        return bookRepository.findBooksByLocationIgnoreCaseAndStatus(location, "APPROVED");
    }

    public List<Book> getAvailableBooksByLocation(String location) {
        return bookRepository.findAvailableBooksByLocation(location);
    }

    public void applyDiscount(Integer ownerId,
                              Integer bookId,
                              Double discountPercentage) {

        User owner = userService.getUserById(ownerId);

        Book book = getBookById(bookId);

        if (!book.getOwnerId().equals(ownerId)) {
            throw new ApiException("Only book owner can apply discount");
        }

        if (owner.getIsBlocked()) {
            throw new ApiException("Blocked users cannot apply discount");
        }

        if (!book.getStatus().equals("APPROVED")) {
            throw new ApiException("Discount can only be applied to approved books");
        }

        if (discountPercentage == null
                || discountPercentage <= 0
                || discountPercentage >= 100) {

            throw new ApiException("Discount percentage must be between 1 and 99");
        }

        Double oldPrice = book.getRentalPrice();

        Double discountedPrice =
                oldPrice - (oldPrice * discountPercentage / 100);

        if (discountedPrice < 0) {
            discountedPrice = 0.0;
        }

        book.setRentalPrice(discountedPrice);

        bookRepository.save(book);
    }

    private Book getBookById(Integer bookId) {

        Book book = bookRepository.findBookByBookId(bookId);

        if (book == null) {
            throw new ApiException("Book not found");
        }

        return book;
    }

}