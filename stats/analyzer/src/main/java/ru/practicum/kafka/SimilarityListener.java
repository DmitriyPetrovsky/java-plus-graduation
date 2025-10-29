package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.handler.SimilarityHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityListener {

    private final SimilarityHandler similarityHandler;

    @KafkaListener(
            topics = "${kafka.topics.similarity}",
            groupId = "${kafka.group-id.similarity}",
            containerFactory = "similarityKafkaListenerContainerFactory"
    )
    public void handleSimilarity(
            @Payload EventSimilarityAvro avro,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        log.info("Получен коэффициент схожести: key={}, value={}", key, avro);

        try {
            similarityHandler.handle(avro);
            ack.acknowledge();
            log.debug("Коэффициент схожести успешно обработан: eventA={}, eventB={}",
                    avro.getEventA(), avro.getEventB());
        } catch (DataIntegrityViolationException e) {
            log.warn("Нарушение целостности данных для коэффициента: {}, ошибка: {}", avro, e.getMessage());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Критическая ошибка при обработке коэффициента схожести: {}", avro, e);
            ack.acknowledge();
        }
    }
}