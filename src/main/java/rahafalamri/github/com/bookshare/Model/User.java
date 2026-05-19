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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @NotEmpty(message = "Name must not be empty")
    @Size(min = 4, message = "Name must be more than 3 letters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Name must contain only letters")
    @Column(columnDefinition = "varchar(30) not null")
    private String name;

    @NotEmpty(message = "Username must not be empty")
    @Size(min = 5, message = "Username must be more than 4 characters")
    @Column(columnDefinition = "varchar(20) not null unique")
    private String username;

    @NotEmpty(message = "Email must not be empty")
    @Email(message = "Email should be valid")
    @Column(columnDefinition = "varchar(50) not null unique")
    private String email;

    @NotEmpty(message = "Phone number must not be empty")
    @Pattern(regexp = "^05\\d{8}$", message = "Phone number must start with 05 and be 10 digits")
    @Column(columnDefinition = "varchar(10) not null")
    private String phoneNumber;

    @NotEmpty(message = "Password must not be empty")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(columnDefinition = "varchar(100) not null")
    private String password;

    @NotEmpty(message = "Role must not be empty")
    @Pattern(regexp = "^(ADMIN|MEMBER)$", message = "Role must be ADMIN or MEMBER")
    @Column(columnDefinition = "varchar(10) not null check (role in ('ADMIN', 'MEMBER'))")
    private String role;

    @Column(columnDefinition = "boolean default false")
    private Boolean isBlocked = false;

    @Column(columnDefinition = "varchar(20) default 'NONE' check (block_type in ('NONE', 'TEMPORARY', 'PERMANENT'))")
    private String blockType = "NONE";

    @PositiveOrZero(message = "Wallet balance cannot be negative")
    @Column(columnDefinition = "double default 0")
    private Double walletBalance = 0.0;

    @Column(columnDefinition = "text")
    private String interests;

    @Column(columnDefinition = "varchar(50) ")
    private String location;
}