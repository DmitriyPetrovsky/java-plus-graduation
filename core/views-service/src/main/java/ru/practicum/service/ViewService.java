package ru.practicum.service;


import ru.practicum.dto.views.ViewStatDto;

public interface ViewService {
    void add(Long eventId, String ip);

    ViewStatDto stat(Long eventId);
}
