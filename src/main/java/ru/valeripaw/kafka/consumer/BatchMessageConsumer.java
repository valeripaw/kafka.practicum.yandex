package ru.valeripaw.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import ru.valeripaw.kafka.properties.ConsumerProperties;
import ru.valeripaw.kafka.properties.KafkaProperties;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.FETCH_MIN_BYTES_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

@Slf4j
public class BatchMessageConsumer implements Closeable, Runnable {

    private final ConsumerProperties consumerProperties;
    private final Consumer<String, String> consumer;

    private boolean stopped = false;

    public BatchMessageConsumer(KafkaProperties kafkaProperties) {
        this.consumerProperties = kafkaProperties.getBatchMessage();

        // Настройка консьюмера – адрес сервера, сериализаторы для ключа и значения
        Properties properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(GROUP_ID_CONFIG, consumerProperties.getGroupId());
        properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Отключаем авто-коммит — управляем вручную
        properties.put(ENABLE_AUTO_COMMIT_CONFIG, consumerProperties.isEnableAutoCommit());

        // Читаем минимум 10 сообщений за poll
        properties.put(MAX_POLL_RECORDS_CONFIG, consumerProperties.getMaxPollRecords());
        // ждать минимум N байт
        properties.put(FETCH_MIN_BYTES_CONFIG, consumerProperties.getFetchMinBytes());
        // ждать до N мс, если мало данных
        properties.put(FETCH_MAX_WAIT_MS_CONFIG, consumerProperties.getFetchMaxWaitMs());

        this.consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(consumerProperties.getTopic()));

        log.info("BatchMessageConsumer подписан на топик {}", consumerProperties.getTopic());
    }

    @Override
    public void run() {
        log.info("BatchMessageConsumer запущен. Ожидание пачек сообщений...");

        long pollDuration = consumerProperties.getPollDurationMs();

        try {
            while (!stopped && !Thread.currentThread().isInterrupted()) {
                readMessage(pollDuration);
            }
        } catch (WakeupException e) {
            // игнорируем при закрытии
            if (!stopped) {
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка в BatchMessageConsumer: {}", e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        log.info("Завершение BatchMessageConsumer");
        stopped = true;

        if (consumer != null) {
            consumer.wakeup();
        }
    }

    private void readMessage(long pollDuration) {
        try {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(pollDuration));

            if (records.isEmpty()) {
                return;
            }

            log.info("Получено {} сообщений для обработки в пачке", records.count());

            for (ConsumerRecord<String, String> record : records) {
                log.info("Получено сообщение: topic={}, partition={}, offset={}, key={}, message={}",
                        record.topic(), record.partition(), record.offset(), record.key(), record.value());

                processMessage(record);
            }

            // Единый коммит после всей пачки
            consumer.commitSync();
            log.info("Коммит оффсета после обработки {} сообщений", records.count());
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
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

}
