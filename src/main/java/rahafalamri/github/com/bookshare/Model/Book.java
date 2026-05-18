package rahafalamri.github.com.bookshare.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookId;

    @NotEmpty(message = "Title must not be empty")
    @Column(columnDefinition = "varchar(100) not null")
    private String title;

    @NotEmpty(message = "Category must not be empty")
    @Column(columnDefinition = "varchar(30) not null")
    private String category;

    @NotEmpty(message = "Description must not be empty")
    @Size(min = 10, message = "Description must be at least 10 characters")
    @Column(columnDefinition = "text not null")
    private String description;

    @Positive(message = "Total copies must be positive")
    @Column(columnDefinition = "int not null")
    private Integer totalCopies;

    @PositiveOrZero(message = "Available copies cannot be negative")
    @Column(columnDefinition = "int not null")
    private Integer availableCopies;

    @PositiveOrZero(message = "Rental price cannot be negative")
    @Column(columnDefinition = "double default 0")
    private Double rentalPrice = 0.0;

    @PositiveOrZero(message = "Deposit amount cannot be negative")
    @Column(columnDefinition = "double default 0")
    private Double depositAmount = 0.0;

    @NotNull(message = "Owner ID is required")
    @Column(columnDefinition = "int not null")
    private Integer ownerId;

    @Column(columnDefinition = "varchar(10) not null check (status in ('PENDING', 'APPROVED', 'REJECTED'))")
    private String status = "PENDING";

    @NotEmpty(message = "Location must not be empty")
    @Column(columnDefinition = "varchar(50) not null")
    private String location;
}