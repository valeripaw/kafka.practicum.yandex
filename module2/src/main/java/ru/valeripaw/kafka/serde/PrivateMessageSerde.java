package ru.valeripaw.kafka.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import ru.valeripaw.kafka.dto.PrivateMessage;

import java.util.Map;

public class PrivateMessageSerde implements Serde<PrivateMessage> {

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
    public Serializer<PrivateMessage> serializer() {
        return (topic, data) -> {
            try {
                return data == null ? null : mapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка сериализации PrivateMessage", e);
            }
        };
    }

    @Override
    public Deserializer<PrivateMessage> deserializer() {
        return (topic, data) -> {
            try {
                return data == null ? null : mapper.readValue(data, PrivateMessage.class);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка десериализации PrivateMessage", e);
            }
        };
    }
}
