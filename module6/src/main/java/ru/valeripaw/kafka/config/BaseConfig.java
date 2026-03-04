package ru.valeripaw.kafka.config;

import ru.valeripaw.kafka.properties.KafkaProperties;

import java.util.Map;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_TOKEN_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG;

public final class BaseConfig {

    private BaseConfig() {

    }

    public static void setUpCommon(Map<String, Object> properties, KafkaProperties kafkaProperties) {
        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(SCHEMA_REGISTRY_URL_CONFIG, kafkaProperties.getSchemaRegistryUrl());
        properties.put("bearer.auth.credentials.source", "STATIC_TOKEN");
        properties.put(BEARER_AUTH_TOKEN_CONFIG, "token");
    }

    public static void setUpSsl(Map<String, Object> properties, KafkaProperties kafkaProperties) {
        // SSL SASL Configuration
        properties.put(SECURITY_PROTOCOL_CONFIG, kafkaProperties.getSecurityProtocol());
        properties.put(SASL_MECHANISM, kafkaProperties.getSaslMechanism());
        properties.put(SASL_JAAS_CONFIG, kafkaProperties.getSaslJaasConfig());
        properties.put(SSL_TRUSTSTORE_LOCATION_CONFIG, kafkaProperties.getTruststorePath());
        properties.put(SSL_TRUSTSTORE_PASSWORD_CONFIG, kafkaProperties.getSslTruststorePassword());
    }

}
