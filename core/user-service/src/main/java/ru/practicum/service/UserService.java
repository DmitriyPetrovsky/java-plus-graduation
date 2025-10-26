package ru.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserNewDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.dto.user.UserUpdateDto;


import java.util.List;

public interface UserService {
    UserDto get(Long id);

    Page<UserDto> get(List<Long> ids, Pageable pageable);

    List<UserDto> get(List<Long> ids);

    UserDto create(UserNewDto user);

    void delete(Long id);

    boolean existsById(Long id);

    boolean checkEmailExist(String email);

    UserDto patch(UserUpdateDto user);

    UserShortDto findShortUserById(Long userId);

    Boolean isUserExistById(Long userId);
}