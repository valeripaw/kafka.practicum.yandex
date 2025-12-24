package ru.valeripaw.kafka.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class SingleMessageConsumer implements AutoCloseable {

    private final Consumer<String, String> consumer;
    private final String topic;

    public SingleMessageConsumer(String bootstrapServers, String groupId, String topic) {
        this.topic = topic;

        // Настройка консьюмера – адрес сервера, сериализаторы для ключа и значения
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Автоматический коммит после обработки каждого сообщения
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");

        // Маленький poll — по одному сообщению
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");

        this.consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(topic));
    }

    public void start() {
        System.out.println("SingleMessageConsumer запущен. Ожидание сообщений...");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("Получено сообщение (авто-коммит): " +
                                    "topic=%s, partition=%d, offset=%d, key=%s, message=%s%n",
                            record.topic(), record.partition(), record.offset(), record.key(), record.value());

                    processMessage(record);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка в SingleMessageConsumer: " + e.getMessage());
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
            consumer.close();
        }
    }

}
