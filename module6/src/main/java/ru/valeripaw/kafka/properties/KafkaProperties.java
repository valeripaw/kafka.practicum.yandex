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
    private String securityProtocol;
    private String saslMechanism;
    private String saslJaasConfig;
    private String sslTruststoreLocation;
    private String sslTruststorePassword;

    @NestedConfigurationProperty
    private ConsumerProperties exampleTopic;
    @NestedConfigurationProperty
    private ProducerProperties exampleEvent;

}
