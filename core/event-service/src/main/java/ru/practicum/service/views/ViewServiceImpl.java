package ru.practicum.service.views;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.views.ViewStatDto;
import ru.practicum.model.views.View;
import ru.practicum.repository.ViewRepository;


@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ViewServiceImpl implements ViewService {
    private final ViewRepository viewRepository;

    @Override
    @Transactional
    public void add(Long eventId, String ip) {
        if (viewRepository.existsByIpAndEventId(ip, eventId)) {
            return;
        }

        View view = new View();
        view.setIp(ip);
        view.setEventId(eventId);
        viewRepository.save(view);
    }

    @Override
    public ViewStatDto stat(Long eventId) {
        Long count = viewRepository.countByEventId(eventId);
        ViewStatDto views = new ViewStatDto();
        views.setEventId(eventId);
        views.setViews(count);
        return views;
    }
}
