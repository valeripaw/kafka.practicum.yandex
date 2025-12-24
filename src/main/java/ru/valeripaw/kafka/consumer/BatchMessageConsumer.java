package ru.valeripaw.kafka.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class BatchMessageConsumer implements AutoCloseable {
    private final Consumer<String, String> consumer;
    private final String topic;

    public BatchMessageConsumer(String bootstrapServers, String groupId, String topic) {
        this.topic = topic;

        // Настройка консьюмера – адрес сервера, сериализаторы для ключа и значения
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Отключаем авто-коммит — управляем вручную
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // Читаем минимум 10 сообщений за poll
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");
        properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1024"); // ждать минимум 1KB
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "500"); // ждать до 500мс, если мало данных

        this.consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(topic));
    }

    public void start() {
        System.out.println("BatchMessageConsumer запущен. Ожидание пачек сообщений...");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                if (records.isEmpty()) {
                    continue;
                }

                System.out.printf("Получено %d сообщений для обработки в пачке%n", records.count());

                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("Получено сообщение (авто-коммит): " +
                                    "topic=%s, partition=%d, offset=%d, key=%s, message=%s%n",
                            record.topic(), record.partition(), record.offset(), record.key(), record.value());

                    processMessage(record);
                }

                // Единый коммит после всей пачки
                consumer.commitSync();
                System.out.printf("Коммит оффсета после обработки %d сообщений%n", records.count());
            }
        } catch (Exception e) {
            System.err.println("Ошибка в BatchMessageConsumer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processMessage(ConsumerRecord<String, String> record) {
        // Имитация обработки
        try {
            Thread.sleep(5); // имитация работы
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        if (consumer != null) {
            try {
                consumer.commitSync(); // финальный коммит
            } catch (Exception e) {
                System.err.println("Ошибка при финальном коммите: " + e.getMessage());
            } finally {
                consumer.close();
            }
        }
    }

}
