package ru.valeripaw.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Добавлен для проверки, что прав на чтение нет.<br>
 * При запуске приложения в логах будет:<br>
 * 2026-03-02T19:54:06.190+04:00  WARN 35501 --- [-listener-0-C-1] org.apache.kafka.clients.NetworkClient   : [Consumer clientId=consumer-topic-2.1-2, groupId=topic-2.1] The metadata response from the cluster reported a recoverable issue with correlation id 3 : {topic-2=TOPIC_AUTHORIZATION_FAILED}<br>
 * 2026-03-02T19:54:06.190+04:00 ERROR 35501 --- [-listener-0-C-1] org.apache.kafka.clients.Metadata        : [Consumer clientId=consumer-topic-2.1-2, groupId=topic-2.1] Topic authorization failed for topics [topic-2]<br>
 * 2026-03-02T19:54:06.190+04:00 ERROR 35501 --- [-listener-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : Authentication/Authorization Exception and no authExceptionRetryInterval set<br>
 * org.apache.kafka.common.errors.TopicAuthorizationException: Not authorized to access topics: [topic-2]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopicTwoConsumer {

    @KafkaListener(
            id = "${kafka.topic-two.topic}-listener",
            idIsGroup = false,
            containerFactory = "topic-two",
            topics = "${kafka.topic-two.topic}",
            autoStartup = "${kafka.topic-two.enable}"
    )
    public void consume(String event) {
        log.info("{}", event);
    }

}
