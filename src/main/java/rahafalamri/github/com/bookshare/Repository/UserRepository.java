package rahafalamri.github.com.bookshare.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rahafalamri.github.com.bookshare.Model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findUserByUserId(Integer userId);

    User findUserByUsername(String username);

    User findUserByEmail(String email);

    @Query("select u from User u where u.isBlocked = true")
    List<User> findBlockedUsers();

    Integer countByIsBlocked(Boolean isBlocked);
}