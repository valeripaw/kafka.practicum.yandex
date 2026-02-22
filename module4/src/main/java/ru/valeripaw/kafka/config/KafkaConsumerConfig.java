package ru.valeripaw.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import ru.valeripaw.kafka.properties.ConsumerProperties;
import ru.valeripaw.kafka.properties.KafkaProperties;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    @Bean("postgres-user")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> postgresUserContainerFactory() {
        return containerFactory(kafkaProperties.getPostgresUser());
    }

    @Bean("postgres-order")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> postgresOrderContainerFactory() {
        return containerFactory(kafkaProperties.getPostgresOrder());
    }

    private ConcurrentKafkaListenerContainerFactory<String, String> containerFactory(
            ConsumerProperties consumerProperties) {
        ConcurrentKafkaListenerContainerFactory<String, String> listener = new ConcurrentKafkaListenerContainerFactory<>();
        listener.setConcurrency(1);
        listener.setConsumerFactory(getConsumerFactory(consumerProperties));
        return listener;
    }

    private ConsumerFactory<String, String> getConsumerFactory(ConsumerProperties consumerProperties) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(GROUP_ID_CONFIG, consumerProperties.getGroupId());
        properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put("schema.registry.url", kafkaProperties.getSchemaRegistryUrl());
        properties.put(ENABLE_AUTO_COMMIT_CONFIG, true);
        properties.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        properties.put(MAX_POLL_RECORDS_CONFIG, 1);

        ConsumerFactory<String, String> factory = new DefaultKafkaConsumerFactory<>(properties);
        return factory;
    }
}
