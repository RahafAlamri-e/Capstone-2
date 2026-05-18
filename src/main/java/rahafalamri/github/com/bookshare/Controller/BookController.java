package rahafalamri.github.com.bookshare.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import rahafalamri.github.com.bookshare.Api.ApiResponse;
import rahafalamri.github.com.bookshare.Model.Book;
import rahafalamri.github.com.bookshare.Service.BookService;

@RestController
@RequestMapping("/api/v1/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/get-all/{adminId}")
    public ResponseEntity<?> getAllBooks(@PathVariable Integer adminId) {
        return ResponseEntity.status(200).body(bookService.getAllBooks(adminId));
    }
    @GetMapping("/get-approve")
    public ResponseEntity<?> getApprovedBooks() {
        return ResponseEntity.status(200).body(bookService.getApprovedBooks());
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<?> getApprovedBooksByLocation(@PathVariable String location) {
        return ResponseEntity.status(200).body(bookService.getApprovedBooksByLocation(location));
    }

    @GetMapping("/get-pending/{adminId}")
    public ResponseEntity<?> getPendingBooks(@PathVariable Integer adminId) {
        return ResponseEntity.status(200).body(bookService.getPendingBooks(adminId));
    }

    @GetMapping("/get-rejected/{adminId}")
    public ResponseEntity<?> getRejectedBooks(@PathVariable Integer adminId) {
        return ResponseEntity.status(200).body(bookService.getRejectedBooks(adminId));
    }

    @GetMapping("/available/location/{location}")
    public ResponseEntity<?> getAvailableBooksByLocation(@PathVariable String location) {
        return ResponseEntity.status(200).body(bookService.getAvailableBooksByLocation(location));
    }

    @PostMapping("/add/{ownerId}")
    public ResponseEntity<?> addBook(@PathVariable Integer ownerId, @RequestBody @Valid Book book, Errors errors) {

        if (errors.hasErrors()) {
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        }

        bookService.addBook(ownerId, book);
        return ResponseEntity.status(201).body(new ApiResponse("Book added successfully and waiting for admin approval"));
    }

    @PutMapping("/update/{ownerId}/{bookId}")
    public ResponseEntity<?> updateBook(@PathVariable Integer ownerId, @PathVariable Integer bookId, @RequestBody @Valid Book book, Errors errors) {

        if (errors.hasErrors()) {
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        }

        bookService.updateBook(ownerId, bookId, book);
        return ResponseEntity.status(200).body(new ApiResponse("Book updated successfully and waiting for approval again"));
    }

    @DeleteMapping("/delete/{userId}/{bookId}")
    public ResponseEntity<?> deleteBook(@PathVariable Integer userId, @PathVariable Integer bookId) {
        bookService.deleteBook(userId, bookId);
        return ResponseEntity.status(200).body(new ApiResponse("Book deleted successfully"));
    }

    @PutMapping("/approve-or-reject/{adminId}/{bookId}/{status}")
    public ResponseEntity<?> approveOrRejectBook(@PathVariable Integer adminId, @PathVariable Integer bookId, @PathVariable String status) {
        bookService.approveOrRejectBook(adminId, bookId, status);
        return ResponseEntity.status(200).body(new ApiResponse("Book status updated successfully"));
    }

    @GetMapping("/search-keyword/{keyword}")
    public ResponseEntity<?> searchApprovedBooksByKeyword(@PathVariable String keyword) {
        return ResponseEntity.status(200).body(bookService.searchApprovedBooksByKeyword(keyword));
    }

    @GetMapping("/owner-book/{ownerId}")
    public ResponseEntity<?> getBooksByOwner(@PathVariable Integer ownerId) {
        return ResponseEntity.status(200).body(bookService.getBooksByOwner(ownerId));
    }

    @GetMapping("/stats/{adminId}")
    public ResponseEntity<?> getPlatformStats(@PathVariable Integer adminId) {
        return ResponseEntity.status(200).body(new ApiResponse(bookService.getPlatformStats(adminId)));
    }

    @PutMapping("/discount/{ownerId}/{bookId}/{discountPercentage}")
    public ResponseEntity<?> applyDiscount(@PathVariable Integer ownerId, @PathVariable Integer bookId, @PathVariable Double discountPercentage) {
        bookService.applyDiscount(ownerId, bookId, discountPercentage);
        return ResponseEntity.status(200).body(new ApiResponse("Discount applied successfully"));
    }
}