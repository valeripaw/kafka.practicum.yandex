package ru.valeripaw.kafka.serde;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;
import java.util.Set;

public class JsonSetSerde implements Serde<Set<String>> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Serde<String> stringSerde = Serdes.String();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        stringSerde.configure(configs, isKey);
    }

    @Override
    public void close() {
        stringSerde.close();
    }

    @Override
    public Serializer<Set<String>> serializer() {
        return (topic, data) -> {
            try {
                return mapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public Deserializer<Set<String>> deserializer() {
        return (topic, data) -> {
            try {
                return mapper.readValue(data, new TypeReference<Set<String>>() {});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}