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
    private String appId;

    @NestedConfigurationProperty
    private ConsumerProperties blockedUser;
    @NestedConfigurationProperty
    private ConsumerProperties privateMessage;
    @NestedConfigurationProperty
    private ProducerProperties censoredMessage;
}
