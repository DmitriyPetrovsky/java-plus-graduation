package ru.practicum.service.location;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.LocationNewDto;
import ru.practicum.dto.location.LocationUpdateDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.model.location.Location;
import ru.practicum.repository.LocationRepository;


import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Override
    public LocationDto getByLonAndLat(Float lon, Float lat) {
        return locationRepository.findByLonAndLat(lon, lat)
                .map(locationMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Место с таким lon-lat не найдено"));
    }

    @Override
    public List<LocationDto> findAll(Integer from, Integer size) {
        Pageable page = PageRequest.of(from, size);
        return locationRepository.findAll(page).stream()
                .map(locationMapper::toDto)
                .toList();
    }

    @Override
    public LocationDto findById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Место с таким id не найдено"));
        return locationMapper.toDto(location);
    }

    @Override
    public List<LocationDto> get(List<Long> ids) {
        return locationRepository.getByIdIn(ids).stream().map(locationMapper::toDto).toList();
    }

    @Override
    @Transactional
    public LocationDto create(LocationNewDto dto) {
        Location location = locationMapper.fromDto(dto);
        return locationMapper.toDto(locationRepository.save(location));
    }

    @Override
    @Transactional
    public LocationDto update(Long id, LocationUpdateDto dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Место с таким id не найдено"));

        location.setId(id);
        location.setLat(dto.getLat());
        location.setLon(dto.getLon());

        return locationMapper.toDto(locationRepository.save(location));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new NotFoundException("Место с таким id не найдено");
        }
        locationRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return locationRepository.existsById(id);
    }

    @Override
    public boolean existsByLonAndLat(Float lon, Float lat) {
        return locationRepository.existsByLonAndLat(lon, lat);
    }
}
