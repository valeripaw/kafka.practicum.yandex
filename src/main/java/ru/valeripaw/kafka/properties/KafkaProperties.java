package ru.valeripaw.kafka.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    private String bootstrapServers;
    private String schemaRegistryUrl;

    @NestedConfigurationProperty
    private ConsumerProperties batchMessage;
    @NestedConfigurationProperty
    private ConsumerProperties singleMessage;
    @NestedConfigurationProperty
    private ProducerProperties exampleEvent;

}
