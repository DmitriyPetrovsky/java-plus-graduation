package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.user.UserDto;

import java.util.List;

@Validated
@FeignClient("user-service")
public interface UserOperations {
    @GetMapping(path = "/api/users/{userId}")
    UserDto getUser(@PathVariable long userId);

    @GetMapping(path = "/api/users/exist/{userId}")
    Boolean isUserExist(@PathVariable long userId);

    @PostMapping("/api/users/getlist")
    List<UserDto> getUsersList(@RequestBody List<Long> ids);
}
