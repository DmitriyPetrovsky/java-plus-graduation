package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.views.ViewStatDto;
import ru.practicum.feign.ViewsOperations;
import ru.practicum.service.ViewService;

@RestController
@RequiredArgsConstructor
public class ViewsApiController implements ViewsOperations {
    private final ViewService viewService;

    @Override
    public void add(Long eventId, String ip) {
        viewService.add(eventId, ip);
    }

    @Override
    public ViewStatDto stat(Long eventId) {
        return viewService.stat(eventId);
    }
}
