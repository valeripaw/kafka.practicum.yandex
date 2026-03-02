package ru.valeripaw.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicOneConsumer {

    @KafkaListener(
            id = "${kafka.topic-one.topic}-listener",
            idIsGroup = false,
            containerFactory = "topic-one",
            topics = "${kafka.topic-one.topic}",
            autoStartup = "${kafka.topic-one.enable}"
    )
    public void consume(String event) {
        log.info("{}", event);
    }
}
