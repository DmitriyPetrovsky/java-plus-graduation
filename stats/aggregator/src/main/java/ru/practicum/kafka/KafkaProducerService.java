package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    public void send(EventSimilarityAvro similarity, String topic) {
        try {
            String key = similarity.getEventA() + "-" + similarity.getEventB();

            CompletableFuture<SendResult<String, EventSimilarityAvro>> future =
                    kafkaTemplate.send(topic, key, similarity);

            future.whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error("Ошибка отправки в топик {}: {}", topic, exception.getMessage());
                } else {
                    log.debug("Сообщение отправлено в топик {}, offset: {}",
                            topic, result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("Критическая ошибка при отправке в топик {}: {}", topic, e.getMessage());
        }
    }
}