package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.LocationNewDto;
import ru.practicum.dto.location.LocationUpdateDto;
import ru.practicum.model.location.Location;


import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDto toDto(Location location);

    Location fromDto(LocationDto locationDto);

    @Mapping(target = "id", ignore = true)
    Location fromDto(LocationNewDto locationDto);

    Location fromDto(LocationUpdateDto locationDto);

    LocationNewDto fromUpdateToNew(LocationUpdateDto locationDto);

    List<Location> fromDto(List<LocationDto> locationDtos);

    List<LocationDto> toDto(List<Location> locations);
}