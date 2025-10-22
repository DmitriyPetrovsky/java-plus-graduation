package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.event.Event;


import java.util.List;

@UtilityClass
public class EventsMapper {

    public static EventDto toDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .categoryId(event.getCategoryId())
                .eventDate(event.getEventDate())
                .initiatorId(event.getInitiatorId())
                .participantLimit(event.getParticipantLimit())
                .confirmedRequests(event.getConfirmedRequests())
                .requestModeration(event.getRequestModeration())
                .paid(event.getPaid())
                .state(event.getState().toString())
                .createdOn(event.getCreatedOn())
                .views(event.getViews())
                .build();
    }

    public static EventShortDto toShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .description(event.getDescription())
                .annotation(event.getAnnotation())
                .categoryId(event.getCategoryId())
                .eventDate(event.getEventDate())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .confirmedRequests(event.getConfirmedRequests())
                .participantLimit(event.getParticipantLimit())
                .initiatorId(event.getInitiatorId())
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .state(event.getTitle())
                .requestModeration(event.getRequestModeration())
                .build();
    }

    public static EventShortDto toShortDto(EventDto event) {
        return EventShortDto.builder()
                .id(event.getId())
                .description(event.getDescription())
                .annotation(event.getAnnotation())
                .categoryId(event.getCategoryId())
                .category(event.getCategory())
                .eventDate(event.getEventDate())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .confirmedRequests(event.getConfirmedRequests())
                .participantLimit(event.getParticipantLimit())
                .initiatorId(event.getInitiatorId())
                .initiator(toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .state(event.getTitle())
                .requestModeration(event.getRequestModeration())
                .build();
    }

    public static Event fromNew(NewEventDto event) {
        return Event.builder()
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .categoryId(event.getCategory())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .confirmedRequests(0L)
                .requestModeration(event.getRequestModeration())
                .eventDate(event.getEventDate())
                .views(0L)
                .build();
    }

    public static List<EventShortDto> toShortDto(List<Event> events) {
        return events.stream().map(EventsMapper::toShortDto).toList();
    }

    public static List<EventShortDto> toShortDtoFromDto(List<EventDto> events) {
        return events.stream().map(EventsMapper::toShortDto).toList();
    }

    private static UserShortDto toUserShortDto(UserDto userDto) {
        UserShortDto userShortDto = new UserShortDto();
        userShortDto.setId(userDto.getId());
        userShortDto.setName(userDto.getName());
        userShortDto.setEmail(userDto.getEmail());
        return userShortDto;
    }
}
