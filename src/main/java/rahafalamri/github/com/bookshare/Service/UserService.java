package rahafalamri.github.com.bookshare.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rahafalamri.github.com.bookshare.Api.ApiException;
import rahafalamri.github.com.bookshare.Model.User;
import rahafalamri.github.com.bookshare.Repository.BookRepository;
import rahafalamri.github.com.bookshare.Repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void addUser(User user) {

        if (userRepository.findUserByUsername(user.getUsername()) != null) {
            throw new ApiException("Username already exists");
        }

        if (userRepository.findUserByEmail(user.getEmail()) != null) {
            throw new ApiException("Email already exists");
        }

        user.setIsBlocked(false);
        user.setBlockType("NONE");

        userRepository.save(user);
    }

    public void updateUser(Integer userId, User user) {

        User oldUser = getUserById(userId);

        User usernameUser = userRepository.findUserByUsername(user.getUsername());

        if (usernameUser != null && !usernameUser.getUserId().equals(userId)) {
            throw new ApiException("Username already exists");
        }

        User emailUser = userRepository.findUserByEmail(user.getEmail());

        if (emailUser != null && !emailUser.getUserId().equals(userId)) {
            throw new ApiException("Email already exists");
        }

        oldUser.setName(user.getName());
        oldUser.setUsername(user.getUsername());
        oldUser.setEmail(user.getEmail());
        oldUser.setPhoneNumber(user.getPhoneNumber());
        oldUser.setPassword(user.getPassword());
        oldUser.setRole(user.getRole());
        oldUser.setInterests(user.getInterests());

        userRepository.save(oldUser);
    }

    public void updateInterests(Integer userId, String interests) {

        User user = getUserById(userId);

        if (interests == null || interests.trim().isEmpty()) {
            throw new ApiException("Interests cannot be empty");
        }

        user.setInterests(interests);

        userRepository.save(user);
    }

    public void deleteUser(Integer userId) {

        User user = getUserById(userId);

        if (user.getRole().equals("ADMIN")) {
            throw new ApiException("Admin account cannot be deleted");
        }

        userRepository.delete(user);
    }

    public void changeBlockStatus(Integer adminId, Integer userId) {

        checkAdmin(adminId);

        User user = getUserById(userId);

        if (user.getRole().equals("ADMIN")) {
            throw new ApiException("Admin account cannot be blocked");
        }

        if (user.getBlockType().equals("PERMANENT")) {
            throw new ApiException("Permanent block cannot be changed from this endpoint");
        }

        if (user.getIsBlocked()) {
            user.setIsBlocked(false);
            user.setBlockType("NONE");
        } else {
            user.setIsBlocked(true);
            user.setBlockType("TEMPORARY");
        }

        userRepository.save(user);
    }

    public List<User> getBlockedUsers(Integer adminId) {

        checkAdmin(adminId);

        return userRepository.findBlockedUsers();
    }

    public void addBalance(Integer userId, Double amount) {

        User user = getUserById(userId);

        if (amount == null || amount <= 0) {
            throw new ApiException("Amount must be positive");
        }

        user.setWalletBalance(user.getWalletBalance() + amount);

        userRepository.save(user);
    }

    public void transferBalance(Integer fromUserId,
                                Integer toUserId,
                                Double amount) {

        User sender = getUserById(fromUserId);

        User receiver = getUserById(toUserId);

        if (sender.getIsBlocked()) {
            throw new ApiException("Blocked users cannot transfer balance");
        }

        if (receiver.getIsBlocked()) {
            throw new ApiException("Cannot transfer balance to blocked user");
        }

        if (fromUserId.equals(toUserId)) {
            throw new ApiException("You cannot transfer balance to yourself");
        }

        if (amount == null || amount <= 0) {
            throw new ApiException("Amount must be positive");
        }

        if (sender.getWalletBalance() < amount) {
            throw new ApiException("Insufficient wallet balance");
        }

        sender.setWalletBalance(sender.getWalletBalance() - amount);

        receiver.setWalletBalance(receiver.getWalletBalance() + amount);

        userRepository.save(sender);

        userRepository.save(receiver);
    }

    public User getUserById(Integer userId) {

        User user = userRepository.findUserByUserId(userId);

        if (user == null) {
            throw new ApiException("User not found");
        }

        return user;
    }

    public void checkAdmin(Integer adminId) {

        User admin = getUserById(adminId);

        if (!admin.getRole().equals("ADMIN")) {
            throw new ApiException("Only admin can do this action");
        }
    }
}