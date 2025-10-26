package ru.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventState;
import ru.practicum.dto.reuqest.ParticipationRequestDto;
import ru.practicum.dto.reuqest.RequestStatus;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.BadConditionsException;
import ru.practicum.exception.DuplicatedDataException;
import ru.practicum.exception.InternalServerException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feign.EventOperations;
import ru.practicum.feign.UserOperations;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.RequestRepository;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserOperations userClient;
    private final RequestMapper requestMapper;
    private final EventOperations eventClient;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        UserDto user;
        try {
            user = userClient.getUser(userId);
        } catch (FeignException e) {
            log.error("Ошибка при обращении к сервису user-service.");
            throw new InternalServerException("Ошибка при обращении к сервису user-service.");
        }
        return requestRepository.findAllByRequesterId(user.getId()).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParticipationRequestDto> getByEventId(Long eventId) {
        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Создание заявки для пользователя: {}, события: {}", userId, eventId);
        EventDto event = new EventDto();
        try {
            event = eventClient.getEventById(eventId);
            log.info("Событие найдено: id={}, state={}, participantLimit={}",
                    event.getId(), event.getState(), event.getParticipantLimit());
        } catch (FeignException e) {
            if (e.status() == 404) {
                log.error("Событие с ID: {} не найдено!", eventId);
                throw new NotFoundException("Событие не найдено");
            }
            if (e.status() >= 500) {
                log.error("Ошибка при обращении к сервису event-service.");
                throw new InternalServerException("Ошибка при обращении к сервису event-service.");
            }
        }

        // Проверка существования заявки
        if (requestRepository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new DuplicatedDataException("Заявка уже существует");
        }

        // Проверка на собственное событие
        if (event.getInitiatorId().equals(userId)) {
            throw new BadConditionsException("Нельзя подавать заявку на своё собственное событие");
        }

        // ОСНОВНОЕ ИСПРАВЛЕНИЕ: Разделяем проверки для событий с лимитом и без лимита
        if (event.getParticipantLimit() != 0) {
            // Для событий с лимитом проверяем публикацию
            if (!event.getState().equals(EventState.PUBLISHED.toString())) {
                throw new BadConditionsException("Нельзя подавать заявку на неопубликованное событие");
            }

            // Проверяем лимит участников
            int requestsConfirmed = requestRepository.findAllByEventIdAndStatus(
                    eventId,
                    RequestStatus.CONFIRMED.toString()).size();

            if (requestsConfirmed >= event.getParticipantLimit()) {
                throw new BadConditionsException("Достигнут лимит участников");
            }
        } else {
            // Для событий без лимита (participantLimit == 0) тоже проверяем публикацию
            if (!event.getState().equals(EventState.PUBLISHED.toString())) {
                throw new BadConditionsException("Нельзя подавать заявку на неопубликованное событие");
            }
        }

        UserDto user;
        try {
            user = userClient.getUser(userId);
        } catch (FeignException e) {
            log.error("Ошибка при обращении к сервису user-service.");
            throw new InternalServerException("Ошибка при обращении к сервису user-service.");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .requesterId(user.getId())
                .eventId(event.getId())
                .status(RequestStatus.PENDING.toString())
                .created(LocalDateTime.now())
                .build();

        // Устанавливаем статус CONFIRMED если не требуется модерация ИЛИ participantLimit == 0
        if (!event.getRequestModeration() || event.getParticipantLimit().equals(0L)) {
            request.setStatus(RequestStatus.CONFIRMED.toString());
        }

        ParticipationRequest res = requestRepository.save(request);
        requestRepository.flush();
        return requestMapper.toDto(res);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка не найдена"));

        UserDto user;
        try {
            user = userClient.getUser(request.getRequesterId());
        } catch (FeignException e) {
            log.error("Ошибка при обращении к сервису user-service.");
            throw new InternalServerException("Ошибка при обращении к сервису user-service.");
        }

        if (!user.getId().equals(userId)) {
            throw new BadConditionsException("Нельзя отменить чужую заявку");
        }

        request.setStatus(RequestStatus.CANCELED.toString());

        ParticipationRequestDto res = requestMapper.toDto(requestRepository.save(request));
        requestRepository.flush();
        return res;
    }

    @Override
    public Map<Long, Long> getConfirmedEventsRequestsCount(List<Long> eventsIds) {
        return requestRepository
                .getCountByEventIdInAndStatus(eventsIds, RequestStatus.CONFIRMED.toString()).stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));
    }

    @Override
    public List<ParticipationRequestDto> findByEventIdAndIdIn(Long eventId, List<Long> requestsId) {
        return requestMapper.toDtoList(requestRepository.findByEventIdAndIdIn(eventId, requestsId));
    }


    @Override
    @Transactional
    public void setStatusAll(List<Long> ids, String status) {
        List<ParticipationRequest> confirmedRequests = requestRepository.findAllByIdInAndStatusIn(
                ids,
                Collections.singletonList(RequestStatus.CONFIRMED.toString())
        );
        if (!confirmedRequests.isEmpty()) {
            throw new BadConditionsException("Нельзя отменить подтверждённую заявку");
        }
        requestRepository.setStatusAll(ids, status);
        requestRepository.flush();
    }

}
