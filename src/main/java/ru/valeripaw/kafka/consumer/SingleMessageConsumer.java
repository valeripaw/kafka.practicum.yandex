package ru.valeripaw.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.valeripaw.kafka.properties.ConsumerProperties;
import ru.valeripaw.kafka.properties.KafkaProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

@Slf4j
@Service
@EnableConfigurationProperties(KafkaProperties.class)
public class SingleMessageConsumer implements AutoCloseable {

    private final ConsumerProperties consumerProperties;
    private final Consumer<String, String> consumer;

    public SingleMessageConsumer(KafkaProperties kafkaProperties) {
        this.consumerProperties = kafkaProperties.getSingleMessage();

        // Настройка консьюмера – адрес сервера, сериализаторы для ключа и значения
        Properties properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(GROUP_ID_CONFIG, consumerProperties.getGroupId());
        properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Автоматический коммит после обработки каждого сообщения
        properties.put(ENABLE_AUTO_COMMIT_CONFIG, consumerProperties.isEnableAutoCommit());
        properties.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, consumerProperties.getAutoCommitIntervalMs());

        // Маленький poll — по одному сообщению
        properties.put(MAX_POLL_RECORDS_CONFIG, consumerProperties.getMaxPollRecords());

        this.consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(consumerProperties.getTopic()));

        log.info("SingleMessageConsumer подписан на топик {}", consumerProperties.getTopic());
    }

    @Async
    public void start() {
        log.info("SingleMessageConsumer запущен. Ожидание сообщений по одному...");

        long pollDuration = consumerProperties.getPollDurationMs();

        try {
            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(pollDuration));

                for (ConsumerRecord<String, String> record : records) {
                    log.info("Получено сообщение (авто-коммит): topic={}, partition={}, offset={}, key={}, message={}",
                            record.topic(), record.partition(), record.offset(), record.key(), record.value());

                    processMessage(record);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка в SingleMessageConsumer: {}", e.getMessage(), e);
        }
    }

    private void processMessage(ConsumerRecord<String, String> record) {
        // Имитация обработки
        try {
            // имитация работы
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        if (consumer != null) {
            consumer.close();
        }
    }

}
