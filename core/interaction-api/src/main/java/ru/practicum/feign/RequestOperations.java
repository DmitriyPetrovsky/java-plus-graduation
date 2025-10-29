package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.reuqest.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

@FeignClient("request-service")
public interface RequestOperations {

    @PostMapping("/api/requests/{eventId}")
    List<ParticipationRequestDto> findByEventIdAndIdIn(@PathVariable Long eventId, @RequestBody List<Long> requestsId);

    @PostMapping("/api/requests/setstatus/{status}")
    void setStatusAll(@RequestBody List<Long> ids, @PathVariable String status);

    @GetMapping("/api/requests/byid/{eventId}")
    List<ParticipationRequestDto> getByEventId(@PathVariable Long eventId);

    @PostMapping("/api/requests/confirmed")
    Map<Long, Long> getConfirmedEventsRequestsCount(@RequestBody List<Long> eventsIds);

    @GetMapping("/{eventId}/{requesterId}/exists")
    boolean isRequestExists(@PathVariable Long requesterId, @PathVariable Long eventId);
}
