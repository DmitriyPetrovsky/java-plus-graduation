package ru.practicum.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.model.EventSimilarity;
import ru.practicum.repository.SimilarityRepository;


@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityHandler {
    private final SimilarityRepository similarityRepository;

    public void handle(EventSimilarityAvro avro) {
        log.info("Сохранение схожести события: {}", avro);

        if (similarityRepository.existsByEventAAndEventB(avro.getEventA(), avro.getEventB())) {
            log.debug("Запись с eventA={} и eventB={} уже есть, пропускаем",
                    avro.getEventA(), avro.getEventB());
            return;
        }

        EventSimilarity similarity = EventSimilarity.builder()
                .eventA(avro.getEventA())
                .eventB(avro.getEventB())
                .score(avro.getScore())
                .timestamp(avro.getTimestamp())
                .build();
        similarityRepository.save(similarity);
    }
}
