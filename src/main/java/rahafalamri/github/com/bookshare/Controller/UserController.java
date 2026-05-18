package rahafalamri.github.com.bookshare.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import rahafalamri.github.com.bookshare.Api.ApiResponse;
import rahafalamri.github.com.bookshare.Model.User;
import rahafalamri.github.com.bookshare.Service.UserService;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.status(200).body(userService.getAllUsers());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid User user, Errors errors) {

        if (errors.hasErrors()) {
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        }

        userService.addUser(user);
        return ResponseEntity.status(201).body(new ApiResponse("User registered successfully"));
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Integer userId, @RequestBody @Valid User user, Errors errors) {

        if (errors.hasErrors()) {
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        }

        userService.updateUser(userId, user);
        return ResponseEntity.status(200).body(new ApiResponse("User updated successfully"));
    }

    @PutMapping("/update-interests/{userId}/{interests}")
    public ResponseEntity<?> updateInterests(@PathVariable Integer userId, @PathVariable String interests) {
        userService.updateInterests(userId, interests);
        return ResponseEntity.status(200).body(new ApiResponse("Interests updated successfully"));
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.status(200).body(new ApiResponse("User deleted successfully"));
    }

    @PutMapping("/change-block/{adminId}/{userId}")
    public ResponseEntity<?> changeBlockStatus(@PathVariable Integer adminId, @PathVariable Integer userId) {
        userService.changeBlockStatus(adminId, userId);
        return ResponseEntity.status(200).body(new ApiResponse("User block status updated successfully"));
    }

    @GetMapping("/blocked-list/{adminId}")
    public ResponseEntity<?> getBlockedUsers(@PathVariable Integer adminId) {
        return ResponseEntity.status(200).body(userService.getBlockedUsers(adminId));
    }

    @PutMapping("/add-balance/{userId}/{amount}")
    public ResponseEntity<?> addBalance(@PathVariable Integer userId, @PathVariable Double amount) {
        userService.addBalance(userId, amount);
        return ResponseEntity.status(200).body(new ApiResponse("Balance added successfully"));
    }

    @PutMapping("/transfer-balance/{fromUserId}/{toUserId}/{amount}")
    public ResponseEntity<?> transferBalance(@PathVariable Integer fromUserId, @PathVariable Integer toUserId, @PathVariable Double amount) {
        userService.transferBalance(fromUserId, toUserId, amount);
        return ResponseEntity.status(200).body(new ApiResponse("Balance transferred successfully"));
    }

}