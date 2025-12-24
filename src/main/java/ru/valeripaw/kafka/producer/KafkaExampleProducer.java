package ru.valeripaw.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaExampleProducer  implements AutoCloseable {

    private final Producer<String, String> producer;
    private final String topic;

    public KafkaExampleProducer(String bootstrapServers, String topic) {
        this.topic = topic;

        // Конфигурация продюсера – адрес сервера, сериализаторы для ключа и значения
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Рекомендуемые настройки для надёжности
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, 3);
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        this.producer = new KafkaProducer<>(properties);
    }

    public void sendMessage(String key, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);

        // Асинхронная отправка с колбэком
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.err.println("Ошибка при отправке сообщения: " + exception.getMessage());
                exception.printStackTrace();
            } else {
                System.out.printf("Сообщение отправлено: topic=%s, partition=%d, offset=%d%n",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    // Блокирующая отправка (опционально)
    public void sendMessageSync(String key, String value) throws ExecutionException, InterruptedException {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        producer.send(record).get(); // ждём подтверждения
    }

    @Override
    public void close() {
        if (producer != null) {
            producer.flush();  // отправить всё, что в буфере
            producer.close();  // корректно завершить работу
        }
    }

}
