package ru.practicum.service.event;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.LocationNewDto;
import ru.practicum.dto.location.LocationUpdateDto;
import ru.practicum.dto.reuqest.ParticipationRequestDto;
import ru.practicum.dto.reuqest.RequestStatus;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.views.ViewStatDto;
import ru.practicum.exception.*;
import ru.practicum.feign.RequestOperations;
import ru.practicum.feign.UserOperations;
import ru.practicum.mapper.EventsMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.QEvent;
import ru.practicum.model.event.StateAction;
import ru.practicum.model.event.StateActionUser;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.category.CategoryService;
import ru.practicum.service.location.LocationService;
import ru.practicum.service.views.ViewService;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final LocationMapper locationMapper;
    private final RequestOperations requestClient;
    private final UserOperations userClient;
    private final CategoryService categoryService;
    private final LocationService locationService;
    private final ViewService viewService;
    private final JPAQueryFactory queryFactory;
    private static final int MINIMAL_MINUTES_FOR_CHANGES = 1;

    @Override
    public EventDto getPublic(Long eventId) {
        return eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .map(this::addInfo)
                .orElseThrow(() -> new NotFoundException("Событие с таким id не найдено"));
    }

    public EventDto getById(Long eventId) {
        return eventRepository.findById(eventId)
                .map(this::addInfo)
                .orElseThrow(() -> new NotFoundException("Событие с таким id не найдено"));
    }

    @Override
    public List<EventDto> get(List<Long> eventIds) {
        return addInfo(eventRepository.findByIdIn(eventIds));
    }


    @Override
    public List<EventDto> getPublished(List<Long> eventIds) {
        return addInfo(eventRepository.findByIdInAndState(eventIds, EventState.PUBLISHED));
    }

    @Override
    public Page<EventShortDto> getByUserId(Long userId, Pageable pageable) {
        Page<Event> page = eventRepository.findAllByInitiatorId(userId, pageable);
        return new PageImpl<>(
                EventsMapper.toShortDtoFromDto(addInfo(page.getContent())),
                pageable,
                page.getTotalElements());
    }

    @Override
    public EventDto getByEventIdAndUserId(Long eventId, Long userId) {
        return EventsMapper.toDto(eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с таким id не найдено ")));
    }

    @Override
    @Transactional
    public EventDto create(Long userId, NewEventDto newEventDto) {
        checkDateIsGoodThrowError(newEventDto.getEventDate());
        try {
            if (!userClient.isUserExist(userId)) {
                throw new NotFoundException("Пользователя с таким id не существует");
            }
        } catch (FeignException e) {
            log.error("Ошибка при обращении к сервису user-service: {}", e.status());
            throw new InternalServerException("Ошибка при обращении к сервису user-service.");
        }

        if (!categoryService.existsById(newEventDto.getCategory())) {
            throw new NotFoundException("Категории с таким id не существует");
        }

        Event event = EventsMapper.fromNew(newEventDto);
        event.setInitiatorId(userId);
        event.setCategoryId(newEventDto.getCategory());
        event.setLocationId(locationService.create(newEventDto.getLocation()).getId());
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        EventDto res = addInfo(eventRepository.save(event));
        eventRepository.flush();
        return res;
    }

    @Override
    public boolean existsById(Long id) {
        return eventRepository.existsById(id);
    }

    @Override
    public boolean checkEventsExistInCategory(Long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }

    @Override
    public boolean existsByIdAndInitiatorId(Long eventId, Long userId) {
        return eventRepository.existsByIdAndInitiatorId(eventId, userId);
    }

    @Override
    @Transactional
    public void increaseViews(Long eventId, String ip) {
        if (!existsById(eventId)) {
            throw new NotFoundException("Событие не найдено");
        }

        viewService.add(eventId, ip);
        ViewStatDto views = viewService.stat(eventId);


        eventRepository.setViews(eventId, views.getViews());
        eventRepository.flush();
    }

    @Override
    public List<EventShortDto> getByFilter(EventFilter filter) {
        checkFilterDateRangeIsGood(filter.getRangeStart(), filter.getRangeEnd());

        List<Event> events;
        QEvent event = QEvent.event;
        BooleanExpression exp = Expressions.asBoolean(true).isTrue();

        if (!filter.getIsDtoForAdminApi()) {
            exp = event.state.eq(EventState.PUBLISHED);
        }

        if (filter.getText() != null && !filter.getText().isBlank()) {
            exp = exp.and(event.description.containsIgnoreCase(filter.getText()))
                    .or(event.annotation.containsIgnoreCase(filter.getText()));
        }

        if (filter.getCategories() != null) {
            exp = exp.and(event.categoryId.in(filter.getCategories()));
        }

        if (filter.getPaid() != null) {
            exp = exp.and(event.paid.eq(filter.getPaid()));
        }

        if (filter.getRangeStart() != null) {
            exp = exp.and(event.eventDate.after(filter.getRangeStart()));
        }

        if (filter.getRangeEnd() != null) {
            exp = exp.and(event.eventDate.before(filter.getRangeEnd()));
        }

        if (filter.getOnlyAvailable() != null) {
            exp = exp.and(event.participantLimit.gt(event.confirmedRequests));
        }

        if (filter.getIsDtoForAdminApi()) {
            if (filter.getStates() != null) {
                List<EventState> eventStates = filter.getStates().stream()
                        .map(EventState::valueOf) // или ваш метод преобразования
                        .collect(Collectors.toList());
                exp = exp.and(event.state.in(eventStates));
            }

            if (filter.getUsers() != null) {
                exp = exp.and(event.initiatorId.in(filter.getUsers()));
            }
        }

        JPAQuery<Event> query = queryFactory.selectFrom(event)
                .where(exp)
                .offset(filter.getFrom())
                .limit(filter.getSize());

        if ("EVENT_DATE".equals(filter.getSort())) {
            events = query.orderBy(event.eventDate.asc()).fetch();
        } else {
            events = query.orderBy(event.views.desc()).fetch();
        }

        return EventsMapper.toShortDtoFromDto(addInfo(events));
    }


    @Override
    @Transactional
    public EventDto updateByAdmin(Long eventId, UpdateEventDto updated) {
        return update(eventId, null, updated);
    }

    @Override
    @Transactional
    public EventDto updateByUser(Long eventId, Long userId, UpdateEventDto updated) {
        return update(eventId, userId, updated);
    }

    @Transactional
    public EventDto update(Long eventId, Long userId, UpdateEventDto updated) {
        boolean isAdminEditThis = userId == null;
        String action = updated.getStateAction();

        checkDateIsGoodThrowError(updated.getEventDate());

        Event event;
        if (isAdminEditThis) {
            event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        } else {
            event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                    .orElseThrow(() -> new NotFoundException("Событие не найдено"));

            if (event.getState().equals(EventState.PUBLISHED)) {
                throw new BadConditionsException("Нельзя изменить уже опубликованное событие");
            }
        }

        checkDateIsGoodThrowError(event.getEventDate());

        if (updated.getCategory() != null) {
            if (!categoryService.existsById(updated.getCategory())) {
                throw new NotFoundException("Категория не найдена");
            }
            event.setCategoryId(updated.getCategory());
        }

        if (updated.getLocation() != null) {
            updateEventLocationOrCreateNew(event, updated.getLocation());
        }

        if (updated.getAnnotation() != null) {
            event.setAnnotation(updated.getAnnotation());
        }

        if (updated.getPaid() != null) {
            event.setPaid(updated.getPaid());
        }

        if (updated.getEventDate() != null) {
            event.setEventDate(updated.getEventDate());
        }

        if (updated.getDescription() != null) {
            event.setDescription(updated.getDescription());
        }

        if (updated.getTitle() != null) {
            event.setTitle(updated.getTitle());
        }

        if (updated.getParticipantLimit() != null) {
            event.setParticipantLimit(updated.getParticipantLimit());
        }

        if (action != null) {
            if (isAdminEditThis) {
                if (action.equals(StateAction.PUBLISH_EVENT.toString())) {
                    if (event.getState().equals(EventState.PUBLISHED)) {
                        throw new BadConditionsException("Нельзя опубликовать уже опубликованное событие");
                    }
                    if (event.getState().equals(EventState.CANCELED)) {
                        throw new BadConditionsException("Нельзя опубликовать уже отменённое событие");
                    }
                    setEventPublished(event);
                } else if (action.equals(StateAction.REJECT_EVENT.toString())) {
                    if (event.getState().equals(EventState.PUBLISHED)) {
                        throw new BadConditionsException("Нельзя отменить уже опубликованное событие");
                    }
                    event.setState(EventState.CANCELED);
                }
            } else {
                if (action.equals(StateActionUser.SEND_TO_REVIEW.toString())) {
                    event.setState(EventState.PENDING);
                } else if (action.equals(StateActionUser.CANCEL_REVIEW.toString())) {
                    event.setState(EventState.CANCELED);
                }
            }
        }

        Event res = eventRepository.save(event);
        eventRepository.flush();
        return addInfo(res);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestsStatus(Long eventId, Long userId,
                                                               EventRequestStatusUpdateRequest updateRequest) {

        getUserAndValidateInitiator(eventId, userId);
        EventDto event = getById(eventId);

        if (!isModerationRequired(event)) {
            return new EventRequestStatusUpdateResult();
        }

        String setStatus = RequestStatus.valueOf(updateRequest.getStatus()).toString();
        List<Long> requestsIds = updateRequest.getRequestIds();
        List<ParticipationRequestDto> requests = getRequestsForEvent(eventId, requestsIds);

        validateRequestsStatus(requests, setStatus);

        if (setStatus.equals(RequestStatus.REJECTED.toString())) {
            return processRejectedRequests(requestsIds, requests);
        } else {
            return processConfirmedRequests(event, requestsIds, requests);
        }
    }

    private void getUserAndValidateInitiator(Long eventId, Long userId) {
        UserDto user;
        try {
            user = userClient.getUser(userId);
        } catch (FeignException e) {
            log.error("Ошибка при обращении к сервису user-service: {}", e.status());
            throw new InternalServerException("Ошибка при обращении к сервису user-service.");
        }

        EventDto event = getById(eventId);
        if (!event.getInitiatorId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "Пользователь может изменять статус только событиям, которые он сделал сам");
        }

    }

    private boolean isModerationRequired(EventDto event) {
        return event.getRequestModeration() && !event.getParticipantLimit().equals(0L);
    }

    private List<ParticipationRequestDto> getRequestsForEvent(Long eventId, List<Long> requestsIds) {
        try {
            return requestClient.findByEventIdAndIdIn(eventId, requestsIds);
        } catch (FeignException e) {
            log.error("Ошибка при обращении к сервису request-service: {}", e.status());
            throw new InternalServerException("Ошибка при обращении к сервису request-service.");
        }
    }

    private void validateRequestsStatus(List<ParticipationRequestDto> requests, String setStatus) {
        final String REJECTED = RequestStatus.REJECTED.toString();

        if (setStatus.equals(REJECTED)) {
            if (requests.stream().anyMatch(x -> x.getStatus().equals(REJECTED))) {
                throw new BadConditionsException(
                        "Статус можно изменить только у заявок, находящихся в состоянии ожидания");
            }
        }
    }

    private EventRequestStatusUpdateResult processRejectedRequests(List<Long> requestsIds,
                                                                   List<ParticipationRequestDto> requests) {
        try {
            requestClient.setStatusAll(requestsIds, RequestStatus.REJECTED.toString());
        } catch (FeignException e) {
            handleRequestServiceError(e);
        }

        requests.forEach(x -> x.setStatus(RequestStatus.REJECTED));
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setRejectedRequests(requests);
        return result;
    }

    private EventRequestStatusUpdateResult processConfirmedRequests(EventDto event,
                                                                    List<Long> requestsIds,
                                                                    List<ParticipationRequestDto> requests) {

        log.info("Проверка лимитов запросов для события с ID: {}, лимит запросов: {}, текущие подтвержденные: {}",
                event.getId(), event.getParticipantLimit(), event.getConfirmedRequests());

        List<ParticipationRequestDto> pendingRequests = getPendingRequests(requests);

        long availableSlots = event.getParticipantLimit() - event.getConfirmedRequests();
        log.info("Available slots: {}", availableSlots);
        if (availableSlots <= 0) {
            throw new BadConditionsException("Достигнут лимит заявок на участие в событии");
        }

        List<ParticipationRequestDto> toConfirm = getRequestsToConfirm(pendingRequests, (int) availableSlots);
        List<ParticipationRequestDto> toReject = getRequestsToReject(pendingRequests, toConfirm);

        confirmSelectedRequests(toConfirm);
        rejectRemainingRequests(toReject);
        updateConfirmedRequestsCount(event, toConfirm);
        return buildResult(new ArrayList<>(), toConfirm, toReject);
    }

    private void updateConfirmedRequestsCount(EventDto event, List<ParticipationRequestDto> toConfirm) {
        long newConfirmedCount = event.getConfirmedRequests() + toConfirm.size();
        setConfirmedRequestsCount(event.getId(), newConfirmedCount);
    }

    private void validateAvailableSlots(EventDto event, List<ParticipationRequestDto> requests, List<ParticipationRequestDto> pendingRequestsToConfirm) {
        long confirmedCount = requests.stream()
                .filter(x -> x.getStatus().equals(RequestStatus.CONFIRMED))
                .count();

        long newConfirmations = pendingRequestsToConfirm.size();
        long totalAfterConfirmation = confirmedCount + newConfirmations;

        if (totalAfterConfirmation > event.getParticipantLimit()) {
            throw new BadConditionsException(
                    "Достигнут лимит заявок на участие в событии. Нельзя подтвердить дополнительные заявки");
        }
    }

    private List<ParticipationRequestDto> getConfirmedRequests(List<ParticipationRequestDto> requests) {
        return requests.stream()
                .filter(x -> x.getStatus().equals(RequestStatus.CONFIRMED))
                .collect(Collectors.toList());
    }

    private List<ParticipationRequestDto> getPendingRequests(List<ParticipationRequestDto> requests) {
        return requests.stream()
                .filter(x -> x.getStatus().equals(RequestStatus.PENDING))
                .toList();
    }

    private int calculateAvailableSlots(EventDto event, List<ParticipationRequestDto> confirmedRequests) {
        int availableSlots = (int) (event.getParticipantLimit() - confirmedRequests.size());
        return Math.max(availableSlots, 0);
    }

    private List<ParticipationRequestDto> getRequestsToConfirm(List<ParticipationRequestDto> pendingRequests,
                                                               int availableSlots) {
        return pendingRequests.subList(0, Math.min(availableSlots, pendingRequests.size()));
    }

    private List<ParticipationRequestDto> getRequestsToReject(List<ParticipationRequestDto> pendingRequests,
                                                              List<ParticipationRequestDto> toConfirm) {
        return pendingRequests.subList(toConfirm.size(), pendingRequests.size());
    }

    private void confirmSelectedRequests(List<ParticipationRequestDto> toConfirm) {
        if (!toConfirm.isEmpty()) {
            List<Long> toConfirmIds = toConfirm.stream()
                    .map(ParticipationRequestDto::getId).toList();

            try {
                requestClient.setStatusAll(toConfirmIds, RequestStatus.CONFIRMED.toString());
            } catch (FeignException e) {
                log.error("Ошибка при обращении к сервису request-service: {}", e.status());
                throw new InternalServerException("Ошибка при обращении к сервису request-service.");
            }

            toConfirm.forEach(x -> x.setStatus(RequestStatus.CONFIRMED));
        }
    }

    private void rejectRemainingRequests(List<ParticipationRequestDto> toReject) {
        if (!toReject.isEmpty()) {
            List<Long> toRejectIds = toReject.stream()
                    .map(ParticipationRequestDto::getId).toList();
            try {
                requestClient.setStatusAll(toRejectIds, RequestStatus.REJECTED.toString());
            } catch (FeignException e) {
                log.error("Ошибка при обращении к сервису request-service.");
                throw new InternalServerException("Ошибка при обращении к сервису request-service.");
            }
        }
    }

    private void updateConfirmedRequestsCount(EventDto event,
                                              List<ParticipationRequestDto> confirmedRequests,
                                              List<ParticipationRequestDto> toConfirm) {
        long newConfirmedCount = confirmedRequests.size() + toConfirm.size();
        setConfirmedRequestsCount(event.getId(), newConfirmedCount);
    }

    private EventRequestStatusUpdateResult buildResult(List<ParticipationRequestDto> confirmedRequests,
                                                       List<ParticipationRequestDto> toConfirm,
                                                       List<ParticipationRequestDto> toReject) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        List<ParticipationRequestDto> allConfirmed = new ArrayList<>(confirmedRequests);
        allConfirmed.addAll(toConfirm);

        result.setConfirmedRequests(allConfirmed);
        result.setRejectedRequests(toReject);

        return result;
    }

    private void handleRequestServiceError(FeignException e) {
        log.error("Ошибка при обращении к сервису request-service: {}", e.status());
        if (e.status() == 409) {
            throw new BadConditionsException("Ошибка запроса к сервису request-service.");
        } else if (e.status() >= 500) {
            throw new InternalServerException("Ошибка при обращении к сервису request-service.");
        }
        throw new InternalServerException("Ошибка при обращении к сервису request-service.");
    }

    @Override
    public List<ParticipationRequestDto> findAllRequestsByEventId(Long eventId, Long userId) {

        try {
            if (!userClient.isUserExist(userId)) {
                throw new NotFoundException("Пользователя с таким id не существует");
            }
        } catch (FeignException e) {
            log.error("Ошибка при обращении к сервису user-service.");
            throw new InternalServerException("Ошибка при обращении к сервису user-service.");
        }


        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("События с таким id не существует");
        }

        List<ParticipationRequestDto> requests;
        try {
            requests = requestClient.getByEventId(eventId);
        } catch (FeignException e) {
            log.error("Ошибка при обращении к сервису request-service.");
            throw new InternalServerException("Ошибка при обращении к сервису request-service.");
        }
        return requests;
    }

    @Override
    public List<EventShortDto> findAllByIds(List<Long> eventIds) {
        return eventRepository.findAllById(eventIds).stream()
                .map(EventsMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void setConfirmedRequestsCount(Long eventId, Long requestsCount) {
        eventRepository.setContirmedRequestsCount(eventId, requestsCount);
        eventRepository.flush();
    }

    private List<EventDto> addInfo(List<Event> events) {
        List<Long> eventsIds = events.stream().map(Event::getId).toList();
        List<Long> categoryIds = events.stream().map(Event::getCategoryId).toList();
        List<Long> usersIds = events.stream().map(Event::getInitiatorId).toList();
        List<Long> locationsIds = events.stream().map(Event::getLocationId).toList();

        List<CategoryDto> categories = categoryService.get(categoryIds);
        List<UserDto> users;
        try {
            users = userClient.getUsersList(usersIds);
        } catch (FeignException e) {
            log.error("Ошибка при обращении к сервису user-service.");
            throw new InternalServerException("Ошибка при обращении к сервису user-service.");
        }
        List<LocationDto> locations = locationService.get(locationsIds);
        Map<Long, Long> requests;
        try {
          requests = requestClient.getConfirmedEventsRequestsCount(eventsIds);
        } catch (FeignException e) {
            log.error("Ошибка при обращении к сервису request-service: {}", e.status());
            throw new BadConditionsException("Ошибка при обращении к сервису request-service.");
        }


        Map<Long, CategoryDto> catsByIds = categories.stream()
                .collect(Collectors.toMap(CategoryDto::getId, Function.identity()));

        Map<Long, UserDto> usersByIds = users.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        Map<Long, LocationDto> locationsByIds = locations.stream()
                .collect(Collectors.toMap(LocationDto::getId, Function.identity()));

        return events.stream()
                .map(e -> {
                    EventDto x = EventsMapper.toDto(e);
                    x.setConfirmedRequests(requests.getOrDefault(x.getId(), 0L));
                    x.setCategory(catsByIds.getOrDefault(x.getCategoryId(), null));
                    x.setInitiator(usersByIds.getOrDefault(x.getInitiatorId(), null));
                    x.setLocation(locationsByIds.getOrDefault(e.getLocationId(), null));
                    return x;
                })
                .toList();
    }

    public EventDto addInfo(Event event) {
        return addInfo(List.of(event)).getFirst();
    }

    @Transactional
    public void updateEventLocationOrCreateNew(Event event, LocationUpdateDto location) {
        Float lon = location.getLon();
        Float lat = location.getLat();

        if (locationService.existsByLonAndLat(lon, lat)) {
            LocationDto locationSaved = locationService.getByLonAndLat(lon, lat);
            event.setLocationId(locationSaved.getId());
        } else {
            LocationNewDto locationNew = locationMapper.fromUpdateToNew(location);
            event.setLocationId(locationService.create(locationNew).getId());
        }
    }

    private void checkDateIsGoodThrowError(LocalDateTime date) {
        if (date == null) {
            return;
        }

        if (!date.isAfter(LocalDateTime.now().plusMinutes(MINIMAL_MINUTES_FOR_CHANGES))) {
            throw new ConstraintViolationException(
                    "Событие можно запланировать или изменить минимум за " + MINIMAL_MINUTES_FOR_CHANGES
                            + " минут до его начала");
        }
    }

    private void checkFilterDateRangeIsGood(LocalDateTime dateBegin, LocalDateTime dateEnd) {
        if (dateBegin == null) {
            return;
        }
        if (dateEnd == null) {
            return;
        }

        if (dateBegin.isAfter(dateEnd)) {
            throw new ConstraintViolationException(
                    "Неверно задана дата начала и конца события в фильтре");
        }
    }

    private void setEventPublished(Event event) {
        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }
}
