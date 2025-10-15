package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.views.ViewStatDto;

@FeignClient("views-service")
public interface ViewsOperations {

    @PostMapping("/api/views/{eventId}")
    void add(@PathVariable Long eventId, @RequestBody String ip);

    @GetMapping("/api/views/{eventId}")
    ViewStatDto stat(@PathVariable Long eventId);
}
