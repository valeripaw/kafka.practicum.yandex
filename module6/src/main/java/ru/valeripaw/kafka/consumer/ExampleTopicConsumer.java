package ru.valeripaw.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.valeripaw.kafka.dto.OrderEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExampleTopicConsumer {

    @KafkaListener(
            id = "${kafka.example-topic.topic}-listener",
            idIsGroup = false,
            containerFactory = "example-topic",
            topics = "${kafka.example-topic.topic}",
            autoStartup = "${kafka.example-topic.enable}"
    )
    public void consume(OrderEvent event) {
        log.info("event {}", event);
    }

}
