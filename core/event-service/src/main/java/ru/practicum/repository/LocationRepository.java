package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.location.Location;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByLonAndLat(Float lon, Float lat);

    List<Location> getByIdIn(List<Long> ids);

    boolean existsByLonAndLat(Float lon, Float lat);
}
