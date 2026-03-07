package ru.valeripaw.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.valeripaw.kafka.dto.OrderEvent;
import ru.valeripaw.kafka.properties.KafkaProperties;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducer {

    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void sendToTopic(OrderEvent message) throws ExecutionException, InterruptedException {
        String topic = kafkaProperties.getExampleEvent().getTopic();
        log.info("Сообщение отправлено: message={}, topic={}", message, topic);
        CompletableFuture<SendResult<String, OrderEvent>> completableFuture = kafkaTemplate.send(topic, message);
        getResult(completableFuture);
    }

    private void getResult(CompletableFuture<SendResult<String, OrderEvent>> completableFuture) throws ExecutionException, InterruptedException {
        RecordMetadata recordMetadata = completableFuture.get().getRecordMetadata();
        log.info("Metadata: {}", toString(recordMetadata));
    }

    private String toString(RecordMetadata recordMetadata) {
        return "RecordMetadata{" +
                "topic=" + recordMetadata.topic() +
                ", offset='" + recordMetadata.offset() + '\'' +
                ", timestamp='" + recordMetadata.timestamp() + '\'' +
                '}';
    }

}
