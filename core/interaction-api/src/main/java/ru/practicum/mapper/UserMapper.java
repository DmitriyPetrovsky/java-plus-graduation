package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserNewDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.dto.user.UserUpdateDto;
import ru.practicum.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    User fromDto(UserDto userDto);

    @Mapping(target = "email", ignore = true)
    UserShortDto toShortFromDto(UserDto userDto);

    @Mapping(target = "id", ignore = true)
    User fromDto(UserNewDto userDto);

    User fromDto(UserUpdateDto userDto);

    List<UserDto> toDto(List<User> users);

    List<User> fromDto(List<UserDto> usersDto);

    default Page<UserDto> toDto(Page<User> users) {
        return users.map(this::toDto);
    }

    UserShortDto toUserShortDto(User user);
}