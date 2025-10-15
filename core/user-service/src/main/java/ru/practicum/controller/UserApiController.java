package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.user.UserDto;
import ru.practicum.feign.UserOperations;
import ru.practicum.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping
@Validated
@RequiredArgsConstructor
public class UserApiController implements UserOperations {

    private final UserService userService;

    @Override
    public UserDto getUser(long userId) {
        log.info("Межсервисное взаимодействие: получение пользователя по ID: " + userId);
        return userService.get(userId);
    }

    @Override
    public Boolean isUserExist(long userId) {
        log.info("Межсервисное взаимодействие: проверка существования пользователя с ID: " + userId);
        return userService.isUserExistById(userId);
    }

    @Override
    public List<UserDto> getUsersList(@RequestBody List<Long> ids) {
        log.info("Межсервисное взаимодействие: получение списка пользователей со следующими ID: {}", ids);
        return userService.get(ids);
    }
}
