package ru.practicum.mapper;



import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.compilation.Compilation;

import java.util.List;

public class CompilationMapper {
    public static CompilationDto toDto(Compilation compilation) {
        List<EventShortDto> events = compilation.getEvents().stream()
                .map(EventsMapper::toShortDto).toList();

        return new CompilationDto(events,
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle());
    }
}
