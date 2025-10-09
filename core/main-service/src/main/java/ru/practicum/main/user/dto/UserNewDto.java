package ru.practicum.main.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNewDto {
    @Size(min = 2, max = 250)
    @NotBlank(message = "Поле name не может быть пустым")
    private String name;

    @Email
    @Size(min = 6, max = 254)
    @NotBlank(message = "Поле email не может быть пустым")
    private String email;
}
