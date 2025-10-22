package ru.practicum.mapper;



import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.reuqest.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {


    @Mapping(source = "eventId", target = "event")
    @Mapping(source = "requesterId", target = "requester")
    ParticipationRequestDto toDto(ParticipationRequest request);

    List<ParticipationRequestDto> toDtoList(List<ParticipationRequest> requests);
}
