package rahafalamri.github.com.bookshare.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer borrowingId;

    @NotNull(message = "Borrower ID is required")
    @Column(columnDefinition = "int not null")
    private Integer userId;

    @NotNull(message = "Book ID is required")
    @Column(columnDefinition = "int not null")
    private Integer bookId;

    @Column(columnDefinition = "date not null")
    private LocalDate borrowDate = LocalDate.now();

    @NotNull(message = "Return date is required")
    @Future(message = "Return date must be in the future")
    @Column(columnDefinition = "date not null")
    private LocalDate returnDate;

    @Column(columnDefinition = "boolean default false")
    private Boolean isReturned = false;

    @Column(columnDefinition = "boolean default false")
    private Boolean returnRequested = false;

    @Column(columnDefinition = "date")
    private LocalDate returnRequestDate;

    @PositiveOrZero(message = "Rental price cannot be negative")
    @Column(columnDefinition = "double default 0")
    private Double rentalPrice = 0.0;

    @PositiveOrZero(message = "Deposit amount cannot be negative")
    @Column(columnDefinition = "double default 0")
    private Double depositAmount = 0.0;


    @PositiveOrZero(message = "Damage fee cannot be negative")
    @Column(columnDefinition = "double default 0")
    private Double damageFee = 0.0;

    @Column(columnDefinition = "varchar(20) check (return_condition in ('GOOD', 'DAMAGED', 'LOST'))")
    private String returnCondition;
}