package ru.practicum.service.compilation;


import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.RequestCompilationCreate;
import ru.practicum.dto.compilation.RequestCompilationUpdate;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> getCompilationList(Boolean pinned, int from, int size);

    CompilationDto getById(long id);

    CompilationDto createCompilation(RequestCompilationCreate requestDto);

    void deleteCompilation(long compId);

    CompilationDto updateCompilation(long compId, RequestCompilationUpdate requestDto);
}
