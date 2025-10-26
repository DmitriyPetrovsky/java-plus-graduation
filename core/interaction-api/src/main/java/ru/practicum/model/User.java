package ru.practicum.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "Поле name не может быть пустым")
    @Size(min = 2, max = 255)
    private String name;

    @Column(unique = true, nullable = false, length = 255)
    @NotBlank(message = "Поле email не может быть пустым")
    @Email
    @Size(min = 5, max = 255)
    private String email;
}
