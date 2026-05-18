package rahafalamri.github.com.bookshare.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rahafalamri.github.com.bookshare.Model.Borrowing;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Integer> {

    Borrowing findBorrowingByBorrowingId(Integer borrowingId);

    List<Borrowing> findBorrowingsByUserId(Integer userId);

    List<Borrowing> findBorrowingsByBookId(Integer bookId);

    Borrowing findBorrowingByUserIdAndBookIdAndIsReturned(Integer userId, Integer bookId, Boolean isReturned);

    List<Borrowing> findBorrowingsByReturnRequestedAndIsReturned(Boolean returnRequested, Boolean isReturned);

    @Query("select b from Borrowing b where b.returnDate < ?1 and b.isReturned = false")
    List<Borrowing> findOverdueBorrowings(LocalDate today);

    @Query("select b from Borrowing b where b.userId = ?1 and b.returnDate < ?2 and b.isReturned = false")
    List<Borrowing> findOverdueBorrowingsByUserId(Integer userId, LocalDate today);

    @Query("select b from Borrowing b where b.returnDate = ?1 and b.isReturned = false")
    List<Borrowing> findBorrowingsDueTomorrow(LocalDate tomorrow);

    @Query("select b from Borrowing b where b.isReturned = false")
    List<Borrowing> findActiveBorrowings();
}