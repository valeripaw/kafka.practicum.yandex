package ru.valeripaw.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConsumer {

    @KafkaListener(
            id = "${kafka.postgres-order.topic}-listener",
            idIsGroup = false,
            containerFactory = "postgres-order",
            topics = "${kafka.postgres-order.topic}",
            autoStartup = "${kafka.postgres-order.enable}"
    )
    public void consume(String event) {
        log.info("{}", event);
    }
}
