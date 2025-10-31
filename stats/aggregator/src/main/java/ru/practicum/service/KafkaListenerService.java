package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.KafkaProducerService;

import java.util.List;


import org.springframework.beans.factory.annotation.Value;


import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaListenerService {

    private final UserActionService userActionService;
    private final KafkaProducerService producerService;

    @Value("${kafka.similarity-topic}")
    private String similarityTopic;

    @KafkaListener(
            topics = "${kafka.action-topic}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(UserActionAvro action, Acknowledgment ack) {
        try {
            log.info("Получено действие пользователя: {}", action);

            List<EventSimilarityAvro> similarities = userActionService.updateSimilarity(action);

            for (EventSimilarityAvro similarity : similarities) {
                producerService.send(similarity, similarityTopic); // ← исправил здесь
            }

            log.info("Действие пользователя {} обработано", action.getUserId());
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Ошибка обработки сообщения: {}", action, e);
        }
    }
}