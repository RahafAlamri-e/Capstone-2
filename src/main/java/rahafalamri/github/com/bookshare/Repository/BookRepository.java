package rahafalamri.github.com.bookshare.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rahafalamri.github.com.bookshare.Model.Book;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    Book findBookByBookId(Integer bookId);

    @Query("select b from Book b where b.status = 'APPROVED'")
    List<Book> findApprovedBooks();

    @Query("select b from Book b where b.status = 'PENDING'")
    List<Book> findPendingBooks();

    @Query("select b from Book b where b.status = 'REJECTED'")
    List<Book> findRejectedBooks();

    @Query("select b from Book b where b.status = 'APPROVED' and " +
            "(lower(b.title) like lower(concat('%', ?1, '%')) or " +
            "lower(b.category) like lower(concat('%', ?1, '%')) or " +
            "lower(b.description) like lower(concat('%', ?1, '%')))")
    List<Book> searchApprovedBooksByKeyword(String keyword);

    List<Book> findBooksByOwnerId(Integer ownerId);

    List<Book> findBooksByLocationIgnoreCaseAndStatus(String location, String status);

    @Query("select b from Book b where lower(b.location) = lower(?1) and b.status = 'APPROVED' and b.availableCopies > 0")
    List<Book> findAvailableBooksByLocation(String location);
}