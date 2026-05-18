package rahafalamri.github.com.bookshare.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rahafalamri.github.com.bookshare.Api.ApiException;
import rahafalamri.github.com.bookshare.Model.Book;
import rahafalamri.github.com.bookshare.Model.Borrowing;
import rahafalamri.github.com.bookshare.Model.User;
import rahafalamri.github.com.bookshare.Repository.BookRepository;
import rahafalamri.github.com.bookshare.Repository.BorrowingRepository;
import rahafalamri.github.com.bookshare.Repository.UserRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final WhatsAppService whatsAppService;

    public List<Borrowing> getAllBorrowings(Integer adminId) {
        userService.checkAdmin(adminId);
        return borrowingRepository.findAll();
    }

    public List<Borrowing> getActiveBorrowings(Integer adminId) {
        userService.checkAdmin(adminId);
        return borrowingRepository.findActiveBorrowings();
    }

    public List<Borrowing> getBorrowingsByUserId(Integer userId) {
        userService.getUserById(userId);
        return borrowingRepository.findBorrowingsByUserId(userId);
    }

    public List<Borrowing> getBorrowingsByBookId(Integer adminId, Integer bookId) {
        userService.checkAdmin(adminId);
        getBookById(bookId);
        return borrowingRepository.findBorrowingsByBookId(bookId);
    }

    public List<Borrowing> getPendingReturnRequests(Integer ownerId) {

        User owner = userService.getUserById(ownerId);

        if (!owner.getRole().equals("MEMBER")) {
            throw new ApiException("Only members can view return requests");
        }

        List<Borrowing> requests =
                borrowingRepository.findBorrowingsByReturnRequestedAndIsReturned(true, false);

        List<Borrowing> result = new ArrayList<>();

        for (Borrowing borrowing : requests) {

            Book book = bookRepository.findBookByBookId(borrowing.getBookId());

            if (book != null && book.getOwnerId().equals(ownerId)) {
                result.add(borrowing);
            }
        }

        return result;
    }

    public void borrowBook(Borrowing borrowing) {

        User borrower = userService.getUserById(borrowing.getUserId());
        Book book = getBookById(borrowing.getBookId());
        User owner = userService.getUserById(book.getOwnerId());

        if (!borrower.getRole().equals("MEMBER")) {
            throw new ApiException("Only members can borrow books");
        }

        if (borrower.getIsBlocked()) {
            throw new ApiException("Blocked users cannot borrow books");
        }

        if (!book.getStatus().equals("APPROVED")) {
            throw new ApiException("Book is not approved yet");
        }

        if (book.getOwnerId().equals(borrower.getUserId())) {
            throw new ApiException("You cannot borrow your own book");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new ApiException("No available copies for this book");
        }

        Borrowing activeSameBook =
                borrowingRepository.findBorrowingByUserIdAndBookIdAndIsReturned(
                        borrowing.getUserId(),
                        borrowing.getBookId(),
                        false
                );

        if (activeSameBook != null) {
            throw new ApiException("You already borrowed this book and did not return it yet");
        }

        long days = ChronoUnit.DAYS.between(LocalDate.now(), borrowing.getReturnDate());

        if (days <= 0 || days > 30) {
            throw new ApiException("Borrowing duration must be from 1 to 30 days");
        }

        Double rentalPrice = book.getRentalPrice();
        Double depositAmount = book.getDepositAmount();
        Double totalAmount = rentalPrice + depositAmount;

        deductBalance(borrower, totalAmount);
        addBalance(owner, rentalPrice);

        borrowing.setBorrowDate(LocalDate.now());
        borrowing.setIsReturned(false);
        borrowing.setReturnRequested(false);
        borrowing.setReturnRequestDate(null);
        borrowing.setRentalPrice(rentalPrice);
        borrowing.setDepositAmount(depositAmount);
        borrowing.setDamageFee(0.0);
        borrowing.setReturnCondition(null);

        book.setAvailableCopies(book.getAvailableCopies() - 1);

        userRepository.save(borrower);
        userRepository.save(owner);
        bookRepository.save(book);
        borrowingRepository.save(borrowing);

        emailService.sendBorrowingTicket(borrower, book, borrowing.getReturnDate().toString());

        String message =
                "Hello " + borrower.getName() + ",\n\n"
                        + "Your borrowing request has been confirmed successfully.\n\n"
                        + "Book: " + book.getTitle() + "\n"
                        + "Return Date: " + borrowing.getReturnDate() + "\n"
                        + "Rental Price: " + rentalPrice + " SAR\n"
                        + "Deposit Amount: " + depositAmount + " SAR\n\n"
                        + "Please return the book on time and in good condition to receive your full deposit refund.\n\n"
                        + "Thank you for using BookShare.";

        whatsAppService.sendWhatsAppMessage(borrower.getPhoneNumber(), message);
    }

    public void updateBorrowing(Integer userId, Integer borrowingId, Borrowing borrowing) {

        userService.getUserById(userId);

        Borrowing oldBorrowing = getBorrowingById(borrowingId);

        if (!oldBorrowing.getUserId().equals(userId)) {
            throw new ApiException("This borrowing does not belong to this user");
        }

        if (oldBorrowing.getIsReturned()) {
            throw new ApiException("Cannot update a returned borrowing");
        }

        if (oldBorrowing.getReturnRequested()) {
            throw new ApiException("Cannot update after return request has been submitted");
        }

        if (borrowing.getReturnDate() == null) {
            throw new ApiException("Return date is required");
        }

        long days = ChronoUnit.DAYS.between(oldBorrowing.getBorrowDate(), borrowing.getReturnDate());

        if (days <= 0 || days > 30) {
            throw new ApiException("Borrowing duration must be from 1 to 30 days");
        }

        oldBorrowing.setReturnDate(borrowing.getReturnDate());

        borrowingRepository.save(oldBorrowing);
    }

    public void deleteBorrowing(Integer adminId, Integer borrowingId) {

        userService.checkAdmin(adminId);

        Borrowing borrowing = getBorrowingById(borrowingId);

        if (!borrowing.getIsReturned()) {
            throw new ApiException("Cannot delete active borrowing record");
        }

        borrowingRepository.delete(borrowing);
    }

    public void requestReturn(Integer userId, Integer borrowingId) {

        User borrower = userService.getUserById(userId);

        Borrowing borrowing = getBorrowingById(borrowingId);

        if (!borrowing.getUserId().equals(userId)) {
            throw new ApiException("This borrowing does not belong to this user");
        }

        if (borrowing.getIsReturned()) {
            throw new ApiException("This book is already returned");
        }

        if (borrowing.getReturnRequested()) {
            throw new ApiException("Return request already submitted");
        }

        borrowing.setReturnRequested(true);

        borrowing.setReturnRequestDate(LocalDate.now());

        borrowingRepository.save(borrowing);

        Book book = getBookById(borrowing.getBookId());

        User owner = userService.getUserById(book.getOwnerId());


        String ownerMessage =
                "Hello " + owner.getName() + ",\n\n"

                        + borrower.getName()
                        + " has submitted a return request for your book.\n\n"

                        + "Book: " + book.getTitle() + "\n\n"

                        + "Please review the book condition and confirm the return through the system.\n\n"

                        + "BookShare Team";

        whatsAppService.sendWhatsAppMessage(
                owner.getPhoneNumber(),
                ownerMessage
        );


        String borrowerMessage =
                "Hello " + borrower.getName() + ",\n\n"

                        + "Your return request has been submitted successfully.\n\n"

                        + "Book: " + book.getTitle() + "\n\n"

                        + "Please wait for the book owner to review the returned book and confirm the final status.\n\n"

                        + "BookShare Team";

        whatsAppService.sendWhatsAppMessage(
                borrower.getPhoneNumber(),
                borrowerMessage
        );
    }

    public void cancelReturnRequest(Integer userId, Integer borrowingId) {

        userService.getUserById(userId);

        Borrowing borrowing = getBorrowingById(borrowingId);

        if (!borrowing.getUserId().equals(userId)) {
            throw new ApiException("This borrowing does not belong to this user");
        }

        if (borrowing.getIsReturned()) {
            throw new ApiException("This book is already returned");
        }

        if (!borrowing.getReturnRequested()) {
            throw new ApiException("There is no return request to cancel");
        }

        if (!LocalDate.now().isBefore(borrowing.getReturnDate())) {
            throw new ApiException("Return request cannot be canceled on or after the return date");
        }

        borrowing.setReturnRequested(false);

        borrowing.setReturnRequestDate(null);

        borrowingRepository.save(borrowing);

        Book book = getBookById(borrowing.getBookId());

        User owner = userService.getUserById(book.getOwnerId());

        User borrower = userService.getUserById(userId);

        String message =
                "Hello " + owner.getName() + ",\n\n"

                        + borrower.getName()
                        + " has canceled the return request for the following book:\n\n"

                        + "Book: " + book.getTitle() + "\n\n"

                        + "BookShare Team";

        whatsAppService.sendWhatsAppMessage(
                owner.getPhoneNumber(),
                message
        );

        String emailMessage =
                "Hello " + borrower.getName() + ",\n\n"
                        + "Your return request has been canceled successfully.\n\n"
                        + "Book: " + book.getTitle() + "\n\n"
                        + "You can submit another return request later if needed.\n\n"
                        + "BookShare Team";

        emailService.sendSimpleEmail(
                borrower.getEmail(),
                "Return Request Canceled",
                emailMessage
        );
    }

    public void confirmReturn(Integer ownerId, Integer borrowingId, String condition, Double damageFee) {

        User owner = userService.getUserById(ownerId);
        Borrowing borrowing = getBorrowingById(borrowingId);

        if (borrowing.getIsReturned()) {
            throw new ApiException("This book is already returned");
        }

        if (!borrowing.getReturnRequested()) {
            throw new ApiException("Borrower must request return first");
        }

        Book book = getBookById(borrowing.getBookId());

        if (!book.getOwnerId().equals(ownerId)) {
            throw new ApiException("Only book owner can confirm return");
        }

        User borrower = userService.getUserById(borrowing.getUserId());

        if (!condition.equals("GOOD") && !condition.equals("DAMAGED") && !condition.equals("LOST")) {
            throw new ApiException("Condition must be GOOD, DAMAGED, or LOST");
        }

        if (damageFee == null || damageFee < 0) {
            throw new ApiException("Damage fee cannot be negative");
        }

        if (damageFee > borrowing.getDepositAmount()) {
            throw new ApiException("Damage fee cannot be greater than deposit amount");
        }

        if (condition.equals("DAMAGED") && damageFee == 0) {
            throw new ApiException("Damage fee is required when condition is DAMAGED");
        }

        LocalDate today = LocalDate.now();
        LocalDate dueDate = borrowing.getReturnDate();
        LocalDate graceDeadline = dueDate.plusDays(7);

        Double deduction = 0.0;

        if (today.isAfter(graceDeadline)) {
            deduction = borrowing.getDepositAmount();
            borrower.setIsBlocked(true);
            borrower.setBlockType("PERMANENT");

        } else if (today.isAfter(dueDate)) {
            deduction = borrowing.getDepositAmount() / 2;
            borrower.setIsBlocked(false);
            borrower.setBlockType("NONE");
        }

        if (condition.equals("DAMAGED")) {
            deduction = Math.max(deduction, damageFee);
        }

        if (condition.equals("LOST")) {
            deduction = borrowing.getDepositAmount();
        }

        Double refund = borrowing.getDepositAmount() - deduction;

        addBalance(borrower, refund);
        addBalance(owner, deduction);

        borrowing.setDamageFee(condition.equals("DAMAGED") ? damageFee : 0.0);
        borrowing.setReturnCondition(condition);
        borrowing.setIsReturned(true);

        book.setAvailableCopies(book.getAvailableCopies() + 1);

        userRepository.save(borrower);
        userRepository.save(owner);
        bookRepository.save(book);
        borrowingRepository.save(borrowing);

        String message;

        if (condition.equals("GOOD")) {
            message =
                    "Hello " + borrower.getName() + ",\n\n"
                            + "Your returned book has been reviewed successfully.\n\n"
                            + "Book: " + book.getTitle() + "\n"
                            + "Condition: GOOD\n"
                            + "Refund Amount: " + refund + " SAR\n\n"
                            + "Thank you for returning the book in good condition.\n\n"
                            + "BookShare Team";

        } else if (condition.equals("DAMAGED")) {
            message =
                    "Hello " + borrower.getName() + ",\n\n"
                            + "Your returned book has been marked as DAMAGED.\n\n"
                            + "Book: " + book.getTitle() + "\n"
                            + "Damage Fee: " + damageFee + " SAR\n"
                            + "Refund Amount: " + refund + " SAR\n\n"
                            + "The damage fee has been deducted from your deposit and transferred to the book owner.\n\n"
                            + "BookShare Team";

        } else {
            message =
                    "Hello " + borrower.getName() + ",\n\n"
                            + "The borrowed book has been marked as LOST.\n\n"
                            + "Book: " + book.getTitle() + "\n\n"
                            + "Your full deposit amount has been transferred to the book owner as compensation.\n\n"
                            + "BookShare Team";
        }

        whatsAppService.sendWhatsAppMessage(borrower.getPhoneNumber(), message);
    }

    public List<Borrowing> getAllOverdueBorrowings(Integer adminId) {
        userService.checkAdmin(adminId);
        return borrowingRepository.findOverdueBorrowings(LocalDate.now());
    }

    public List<Borrowing> getUserOverdueBorrowings(Integer userId) {
        userService.getUserById(userId);
        return borrowingRepository.findOverdueBorrowingsByUserId(userId, LocalDate.now());
    }

    public void sendReturnReminders(Integer adminId) {

        userService.checkAdmin(adminId);

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Borrowing> borrowings =
                borrowingRepository.findBorrowingsDueTomorrow(tomorrow);

        for (Borrowing borrowing : borrowings) {

            User borrower = userRepository.findUserByUserId(borrowing.getUserId());
            Book book = bookRepository.findBookByBookId(borrowing.getBookId());

            if (borrower != null && book != null) {

                String message =
                        "Hello " + borrower.getName() + ",\n\n"
                                + "This is a friendly reminder that your borrowed book is due tomorrow.\n\n"
                                + "Book: " + book.getTitle() + "\n"
                                + "Due Date: " + borrowing.getReturnDate() + "\n\n"
                                + "Please return the book on time to avoid any deposit deductions or account restrictions.\n\n"
                                + "BookShare Team";

                whatsAppService.sendWhatsAppMessage(borrower.getPhoneNumber(), message);
            }
        }
    }

    public void checkOverdueAndBlockUsers(Integer adminId) {

        userService.checkAdmin(adminId);

        List<Borrowing> overdueBorrowings =
                borrowingRepository.findOverdueBorrowings(LocalDate.now());

        for (Borrowing borrowing : overdueBorrowings) {

            User borrower = userRepository.findUserByUserId(borrowing.getUserId());
            Book book = bookRepository.findBookByBookId(borrowing.getBookId());

            if (borrower != null && !borrower.getIsBlocked()) {

                borrower.setIsBlocked(true);
                borrower.setBlockType("TEMPORARY");

                userRepository.save(borrower);

                String bookTitle = book != null ? book.getTitle() : "borrowed book";

                String message =
                        "Hello " + borrower.getName() + ",\n\n"
                                + "Your account has been temporarily blocked because the borrowing period has ended and the book has not been returned yet.\n\n"
                                + "Book: " + bookTitle + "\n"
                                + "Grace Period: 7 days\n\n"
                                + "Please return the book during the grace period to avoid permanent blocking and full deposit deduction.\n\n"
                                + "BookShare Team";

                whatsAppService.sendWhatsAppMessage(borrower.getPhoneNumber(), message);
            }
        }
    }

    private Book getBookById(Integer bookId) {

        Book book = bookRepository.findBookByBookId(bookId);

        if (book == null) {
            throw new ApiException("Book not found");
        }

        return book;
    }

    private Borrowing getBorrowingById(Integer borrowingId) {

        Borrowing borrowing = borrowingRepository.findBorrowingByBorrowingId(borrowingId);

        if (borrowing == null) {
            throw new ApiException("Borrowing record not found");
        }

        return borrowing;
    }

    private void addBalance(User user, Double amount) {
        user.setWalletBalance(user.getWalletBalance() + amount);
    }

    private void deductBalance(User user, Double amount) {

        if (user.getWalletBalance() < amount) {
            throw new ApiException("Insufficient wallet balance");
        }

        user.setWalletBalance(user.getWalletBalance() - amount);
    }
}