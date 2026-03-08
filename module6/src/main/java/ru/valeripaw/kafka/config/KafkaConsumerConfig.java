package ru.valeripaw.kafka.config;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import ru.valeripaw.kafka.dto.OrderEvent;
import ru.valeripaw.kafka.properties.ConsumerProperties;
import ru.valeripaw.kafka.properties.KafkaProperties;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static ru.valeripaw.kafka.config.BaseConfig.setUpCommon;
import static ru.valeripaw.kafka.config.BaseConfig.setUpSsl;

@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    @Value("${local-env}")
    private boolean localEnv;

    @Bean("example-topic")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, OrderEvent>> exampleTopicContainerFactory() {
        return containerFactory(kafkaProperties.getExampleTopic());
    }

    private ConcurrentKafkaListenerContainerFactory<String, OrderEvent> containerFactory(
            ConsumerProperties consumerProperties) {
        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> listener = new ConcurrentKafkaListenerContainerFactory<>();
        listener.setConcurrency(1);
        listener.setConsumerFactory(getConsumerFactory(consumerProperties));
        return listener;
    }

    private ConsumerFactory<String, OrderEvent> getConsumerFactory(ConsumerProperties consumerProperties) {
        Map<String, Object> properties = new HashMap<>();

        setUpCommon(properties, kafkaProperties);

        properties.put(GROUP_ID_CONFIG, consumerProperties.getGroupId());
        properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        properties.put("specific.avro.reader", true);

        // Автоматический коммит после обработки каждого сообщения
        properties.put(ENABLE_AUTO_COMMIT_CONFIG, consumerProperties.isEnableAutoCommit());
        properties.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, consumerProperties.getAutoCommitIntervalMs());

        // Маленький poll — по одному сообщению
        properties.put(MAX_POLL_RECORDS_CONFIG, consumerProperties.getMaxPollRecords());

        properties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");

        if (!localEnv) {
            setUpSsl(properties, kafkaProperties);
        }

        return new DefaultKafkaConsumerFactory<>(properties);
    }
}
