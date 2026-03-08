package ru.valeripaw.kafka.config;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.valeripaw.kafka.dto.OrderEvent;
import ru.valeripaw.kafka.properties.KafkaProperties;
import ru.valeripaw.kafka.properties.ProducerProperties;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.RETRY_BACKOFF_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static ru.valeripaw.kafka.config.BaseConfig.setUpCommon;
import static ru.valeripaw.kafka.config.BaseConfig.setUpSsl;

@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;

    @Value("${local-env}")
    private boolean localEnv;

    @Bean
    public KafkaTemplate<String, OrderEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    public ProducerFactory<String, OrderEvent> producerFactory() {
        ProducerProperties producerProperties = kafkaProperties.getExampleEvent();

        Map<String, Object> properties = new HashMap<>();
        setUpCommon(properties, kafkaProperties);

        properties.put("auto.register.schemas", true);

        properties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());

        // Рекомендуемые настройки для надёжности
        properties.put(ACKS_CONFIG, producerProperties.getAcks());
        // Кол-во попыток
        properties.put(RETRIES_CONFIG, producerProperties.getRetries());
        // Задержка между попытками
        properties.put(RETRY_BACKOFF_MS_CONFIG, producerProperties.getRetryBackoffMs());
        properties.put(ENABLE_IDEMPOTENCE_CONFIG, producerProperties.isEnableIdempotence());

        if (!localEnv) {
            setUpSsl(properties, kafkaProperties);
        }

        return new DefaultKafkaProducerFactory<>(properties);
    }

}
