package ru.practicum.main.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentDto {
    @NotBlank
    @Size(min = 1, max = 1000)
    private String text;
}
