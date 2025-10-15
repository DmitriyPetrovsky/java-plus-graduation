package ru.practicum.service.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.RequestCompilationCreate;
import ru.practicum.dto.compilation.RequestCompilationUpdate;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.compilation.Compilation;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CompilationDto> getCompilationList(Boolean pinned, int from, int size) {
        Stream<Compilation> compilationStream = compilationRepository.findAll().stream();

        if (pinned != null) {
            compilationStream = compilationStream.filter(c -> c.getPinned() == pinned);
        }
        List<CompilationDto> result = compilationStream
                .skip(from)
                .limit(size)
                .map(CompilationMapper::toDto)
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public CompilationDto getById(long id) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена"));
        return CompilationMapper.toDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(RequestCompilationCreate requestDto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(requestDto.getTitle());
        compilation.setPinned(Optional.ofNullable(requestDto.getPinned()).orElse(false));
        if (requestDto.getEvents() != null) {
            compilation.setEvents(eventRepository.findAllById(requestDto.getEvents()));
        } else {
            compilation.setEvents(new ArrayList<>());
        }

        Compilation res = compilationRepository.save(compilation);
        compilationRepository.flush();
        return CompilationMapper.toDto(res);
    }

    @Override
    @Transactional
    public void deleteCompilation(long compId) {
        compilationRepository.deleteById(compId);
        compilationRepository.flush();
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(long compId, RequestCompilationUpdate requestDto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена"));
        if (requestDto.getPinned() != null) {
            compilation.setPinned(requestDto.getPinned());
        }
        if (requestDto.getTitle() != null) {
            compilation.setTitle(requestDto.getTitle());
        }
        if (requestDto.getEvents() != null) {
            compilation.setEvents(eventRepository.findAllById(requestDto.getEvents()));
        }

        Compilation res = compilationRepository.save(compilation);
        compilationRepository.flush();
        return CompilationMapper.toDto(res);
    }
}
