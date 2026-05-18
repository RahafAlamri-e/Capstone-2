package rahafalamri.github.com.bookshare.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import rahafalamri.github.com.bookshare.Api.ApiResponse;
import rahafalamri.github.com.bookshare.Model.Borrowing;
import rahafalamri.github.com.bookshare.Service.BorrowingService;

@RestController
@RequestMapping("/api/v1/borrow")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    @GetMapping("/get-all/{adminId}")
    public ResponseEntity<?> getAllBorrowings(@PathVariable Integer adminId) {
        return ResponseEntity.status(200).body(borrowingService.getAllBorrowings(adminId));
    }

    @GetMapping("/active/{adminId}")
    public ResponseEntity<?> getActiveBorrowings(@PathVariable Integer adminId) {
        return ResponseEntity.status(200).body(borrowingService.getActiveBorrowings(adminId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBorrowingsByUserId(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(borrowingService.getBorrowingsByUserId(userId));
    }

    @GetMapping("/book/{adminId}/{bookId}")
    public ResponseEntity<?> getBorrowingsByBookId(@PathVariable Integer adminId, @PathVariable Integer bookId) {
        return ResponseEntity.status(200).body(borrowingService.getBorrowingsByBookId(adminId, bookId));
    }

    @PostMapping("/request")
    public ResponseEntity<?> borrowBook(@RequestBody @Valid Borrowing borrowing, Errors errors) {

        if (errors.hasErrors()) {
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        }

        borrowingService.borrowBook(borrowing);
        return ResponseEntity.status(201).body(new ApiResponse("Borrowing completed successfully"));
    }

    @PutMapping("/update/{userId}/{borrowingId}")
    public ResponseEntity<?> updateBorrowing(@PathVariable Integer userId, @PathVariable Integer borrowingId, @RequestBody @Valid Borrowing borrowing, Errors errors) {

        if (errors.hasErrors()) {
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        }

        borrowingService.updateBorrowing(userId, borrowingId, borrowing);
        return ResponseEntity.status(200).body(new ApiResponse("Borrowing updated successfully"));
    }

    @DeleteMapping("/delete/{adminId}/{borrowingId}")
    public ResponseEntity<?> deleteBorrowing(@PathVariable Integer adminId, @PathVariable Integer borrowingId) {

        borrowingService.deleteBorrowing(adminId, borrowingId);
        return ResponseEntity.status(200).body(new ApiResponse("Borrowing deleted successfully"));
    }


    @GetMapping("/return-requests/{ownerId}")
    public ResponseEntity<?> getPendingReturnRequests(@PathVariable Integer ownerId) {
        return ResponseEntity.status(200).body(borrowingService.getPendingReturnRequests(ownerId));
    }


    @PutMapping("/return-request/{userId}/{borrowingId}")
    public ResponseEntity<?> requestReturn(@PathVariable Integer userId, @PathVariable Integer borrowingId) {
        borrowingService.requestReturn(userId, borrowingId);
        return ResponseEntity.status(200).body(new ApiResponse("Return request submitted successfully"));
    }

    @PutMapping("/cancel-return-request/{userId}/{borrowingId}")
    public ResponseEntity<?> cancelReturnRequest(@PathVariable Integer userId, @PathVariable Integer borrowingId) {
        borrowingService.cancelReturnRequest(userId, borrowingId);
        return ResponseEntity.status(200).body(new ApiResponse("Return request canceled successfully"));
    }

    @PutMapping("/confirm-return/{ownerId}/{borrowingId}/{condition}/{damageFee}")
    public ResponseEntity<?> confirmReturn(@PathVariable Integer ownerId, @PathVariable Integer borrowingId, @PathVariable String condition, @PathVariable Double damageFee) {
        borrowingService.confirmReturn(ownerId, borrowingId, condition, damageFee);
        return ResponseEntity.status(200).body(new ApiResponse("Return confirmed successfully"));
    }

    @GetMapping("/overdue/all/{adminId}")
    public ResponseEntity<?> getAllOverdueBorrowings(@PathVariable Integer adminId) {
        return ResponseEntity.status(200).body(borrowingService.getAllOverdueBorrowings(adminId));
    }

    @GetMapping("/overdue/user/{userId}")
    public ResponseEntity<?> getUserOverdueBorrowings(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(borrowingService.getUserOverdueBorrowings(userId));
    }

    @PutMapping("/send-reminders/{adminId}")
    public ResponseEntity<?> sendReturnReminders(@PathVariable Integer adminId) {
        borrowingService.sendReturnReminders(adminId);
        return ResponseEntity.status(200).body(new ApiResponse("Return reminders sent successfully"));
    }

    @PutMapping("/check-overdue/{adminId}")
    public ResponseEntity<?> checkOverdueAndBlockUsers(@PathVariable Integer adminId) {
        borrowingService.checkOverdueAndBlockUsers(adminId);
        return ResponseEntity.status(200).body(new ApiResponse("Overdue borrowings checked successfully"));
    }

}