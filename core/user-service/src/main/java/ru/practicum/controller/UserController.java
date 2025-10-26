package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserNewDto;
import ru.practicum.dto.user.UserUpdateDto;
import ru.practicum.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/users")
@Validated
public class UserController {
    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> get(@RequestParam(required = false) List<Long> ids,
                             @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                             @RequestParam(defaultValue = "10") @Positive Integer size) {
        Sort sort = Sort.unsorted();
        PageRequest pageable = PageRequest.of(from, size, sort);
        return userService.get(ids, pageable).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto add(@Valid @RequestBody UserNewDto user) {
        return userService.create(user);
    }

    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto patch(@PathVariable @Positive Long userId,
                    @Valid @RequestBody UserUpdateDto user) {
        user.setId(userId);
        return userService.patch(user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        userService.delete(id);
    }
}
