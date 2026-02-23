package ru.valeripaw.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserConsumer {

    @KafkaListener(
            id = "${kafka.postgres-user.topic}-listener",
            idIsGroup = false,
            containerFactory = "postgres-user",
            topics = "${kafka.postgres-user.topic}",
            autoStartup = "${kafka.postgres-user.enable}"
    )
    public void consume(String event) {
        log.info("{}", event);
    }

}
