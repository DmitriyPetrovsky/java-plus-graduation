package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserNewDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.dto.user.UserUpdateDto;
import ru.practicum.exception.DuplicatedDataException;
import ru.practicum.exception.NotFoundException;

import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;


import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto get(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не найден"));
    }

    @Override
    public List<UserDto> get(List<Long> ids) {
        return userRepository.findByIdIn(ids).stream().map(userMapper::toDto).toList();
    }

    @Override
    public Page<UserDto> get(List<Long> ids, Pageable pageable) {
        if (ids == null || ids.isEmpty()) {
            return userMapper.toDto(userRepository.findAll(pageable));
        }
        return userMapper.toDto(userRepository.findByIdIn(ids, pageable));
    }

    @Override
    @Transactional
    public UserDto create(UserNewDto userDto) {
        User user = userMapper.fromDto(userDto);

        if (checkEmailExist(user.getEmail())) {
            throw new DuplicatedDataException("Пользователь с таким email уже существует");
        }

        if (user.getName() != null && user.getName().isBlank()) {
            user.setName("Unnamed");
        }

        User userSaved = userRepository.save(user);
        userRepository.flush();
        return userMapper.toDto(userSaved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
        userRepository.flush();
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public boolean checkEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public UserDto patch(UserUpdateDto userDto) {
        User user = userMapper.fromDto(userDto);

        if (!userRepository.existsById(user.getId())) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }

        if (user.getEmail() != null && checkEmailExist(user.getEmail())) {
            throw new DuplicatedDataException("Пользователь с таким email уже существует");
        }

        Long id = user.getId();
        User userSaved = userRepository.findById(id).orElseThrow(() -> new NotFoundException("пользователь не найден по id"));

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            userSaved.setEmail(user.getEmail());
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            userSaved.setName(user.getName());
        }

        User userLoad = userRepository.save(userSaved);
        userRepository.flush();
        return userMapper.toDto(userLoad);
    }

    @Override
    public UserShortDto findShortUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("пользователь не найден по id"));
        return userMapper.toUserShortDto(user);
    }

    @Override
    public Boolean isUserExistById(Long userId) {
        return userRepository.existsById(userId);
    }
}