package ru.practicum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNewDto {
    @NotBlank(message = "Поле name не может быть пустым")
    @Size(min = 2, max = 250)
    private String name;

    @NotBlank(message = "Поле email не может быть пустым")
    @Email
    @Size(min = 6, max = 254)
    private String email;
}
