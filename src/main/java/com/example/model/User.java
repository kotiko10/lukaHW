package com.example.model;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Login is required")
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    @Column(name = "login", nullable = false, unique = true, length = 50)
    private String login;
    @NotBlank(message = "Password is required")
    @Size(min = 3, message = "Password must be at least 3 characters long")
    @Column(name = "password", nullable = false)
    private String password;
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    @Past(message = "Birthday must be in the past")
    @Column(name = "birthday")
    private LocalDate birthday;

    @ManyToOne(fetch = FetchType.EAGER) // gadaxede defaultebs
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    public User(String login, String password, String email, String firstName, String lastName, LocalDate birthday, Role role) {
        this.login = login;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.role = role;
    }
}
