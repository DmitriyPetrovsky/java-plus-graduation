package ru.practicum.dto.event;



import java.time.LocalDateTime;
import java.util.List;

public record EventSearchParameters(List<Long> usersIds, List<EventState> eventStates, List<Long> categoriesIds,
                                    LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
}
