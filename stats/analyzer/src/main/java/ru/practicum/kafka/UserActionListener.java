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
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.handler.UserActionHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionListener {

    private final UserActionHandler userActionHandler;

    @KafkaListener(
            topics = "${kafka.topics.action}",
            groupId = "${kafka.group-id.action}",
            containerFactory = "userActionKafkaListenerContainerFactory"
    )
    public void handleUserAction(
            @Payload UserActionAvro avro,
            @Header(KafkaHeaders.RECEIVED_KEY) Long key,
            Acknowledgment ack) {

        log.info("Получено действие пользователя: key={}, value={}", key, avro);

        try {
            userActionHandler.handle(avro);
            ack.acknowledge();
            log.debug("Действие пользователя успешно обработано: userId={}, eventId={}",
                    avro.getUserId(), avro.getEventId());
        } catch (DataIntegrityViolationException e) {
            log.warn("Нарушение целостности данных для действия: {}, ошибка: {}", avro, e.getMessage());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Критическая ошибка при обработке действия пользователя: {}", avro, e);

            ack.acknowledge();
        }
    }
}