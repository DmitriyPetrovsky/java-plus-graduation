package ru.practicum.dto.user;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDto {
    private Long id;

    @NotBlank(message = "Поле name не может быть пустым")
    @Size(min = 2, max = 255)
    private String name;

    @NotBlank(message = "Поле email не может быть пустым")
    @Email
    @Size(min = 5, max = 255)
    private String email;
}
