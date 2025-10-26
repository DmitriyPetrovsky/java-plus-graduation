package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.reuqest.ParticipationRequestDto;
import ru.practicum.feign.RequestOperations;
import ru.practicum.service.RequestService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class RequestApiController implements RequestOperations {

    private final RequestService requestService;

    @Override
    public List<ParticipationRequestDto> findByEventIdAndIdIn(Long eventId, List<Long> requestsId) {
        log.info("Межсервисное взаимодействие: получение списка запросов для события с ID: {}", eventId);
        return requestService.findByEventIdAndIdIn(eventId, requestsId);
    }

    @Override
    public void setStatusAll(List<Long> ids, String status) {
        log.info("Межсервисное взаимодействие: установка статуса запросов с ID {} на {}", ids, status);
        requestService.setStatusAll(ids, status);
    }

    @Override
    public List<ParticipationRequestDto> getByEventId(@PathVariable Long eventId) {
        log.info("Межсервисное взаимодействие: получение списка запросов для события с ID: {}", eventId);
        return requestService.getByEventId(eventId);
    }

    @Override
    public Map<Long, Long> getConfirmedEventsRequestsCount(List<Long> eventsIds) {
        log.info("Межсервисное взаимодействие: получение количества подтверждённых заявок");
        return requestService.getConfirmedEventsRequestsCount(eventsIds);
    }

}
