package ru.valeripaw.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import ru.valeripaw.kafka.properties.KafkaProperties;
import ru.valeripaw.kafka.properties.ProducerProperties;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

@Slf4j
@Service
@EnableConfigurationProperties(KafkaProperties.class)
public class ExampleEvenProducer implements AutoCloseable {

    private final ProducerProperties producerProperties;
    private final Producer<String, String> producer;

    public ExampleEvenProducer(KafkaProperties kafkaProperties) {
        this.producerProperties = kafkaProperties.getExampleEvent();

        // Конфигурация продюсера – адрес сервера, сериализаторы для ключа и значения
        Properties properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Рекомендуемые настройки для надёжности
        properties.put(ACKS_CONFIG, producerProperties.getAcks());
        properties.put(RETRIES_CONFIG, producerProperties.getRetries());
        properties.put(ENABLE_IDEMPOTENCE_CONFIG, producerProperties.isEnableIdempotence());

        this.producer = new KafkaProducer<>(properties);
    }

    public void sendMessage(String key, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(producerProperties.getTopic(), key, message);

        // Асинхронная отправка с колбэком
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке сообщения: {}", exception.getMessage(), exception);
            } else {
                log.info("Сообщение отправлено: topic={}, partition={}, offset={}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    // Блокирующая отправка (опционально)
    public void sendMessageSync(String key, String value) throws ExecutionException, InterruptedException {
        ProducerRecord<String, String> record = new ProducerRecord<>(producerProperties.getTopic(), key, value);
        // ждём подтверждения
        producer.send(record).get();
    }

    @Override
    public void close() {
        if (producer != null) {
            // отправить всё, что в буфере
            producer.flush();
            // корректно завершить работу
            producer.close();
        }
    }

}
