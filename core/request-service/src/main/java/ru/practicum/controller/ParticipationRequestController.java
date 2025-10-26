package ru.practicum.controller;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.reuqest.ParticipationRequestDto;
import ru.practicum.service.RequestService;


import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/users/{userId}/requests")
@Validated
public class ParticipationRequestController {
    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive Long userId) {
        return requestService.getUserRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(
            @PathVariable @Positive Long userId,
            @RequestParam @Positive Long eventId) {
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
            @PathVariable @Positive Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }
}
