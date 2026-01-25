package ru.valeripaw.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.valeripaw.kafka.dto.PrivateMessage;
import ru.valeripaw.kafka.dto.Tmp;
import ru.valeripaw.kafka.properties.KafkaProperties;
import ru.valeripaw.kafka.serde.JsonSetSerde;
import ru.valeripaw.kafka.serde.PrivateMessageSerde;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.apache.kafka.streams.StreamsConfig.*;

/**
 * Подзадача 1.  Блокировка нежелательных пользователей
 * Реализуйте поток обработки сообщений, в котором пользователи смогут блокировать других пользователей. Для этого:
 * - Создайте отдельный список заблокированных для каждого пользователя.
 * - Этот список должен храниться в персистентном хранилище: persistent state store
 * Реализуйте автоматическую фильтрацию: сообщения от нежелательных пользователей не должны доходить до получателей.
 */
@Slf4j
public class BlockingService implements Closeable, Runnable {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final KafkaStreams streams;

    public BlockingService(KafkaProperties kafkaProperties) {
        // Настройка свойств Kafka Streams
        Properties properties = new Properties();
        properties.put(APPLICATION_ID_CONFIG, kafkaProperties.getAppId());
        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // Включаем кэширование для GlobalKTable
        properties.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 100 * 1024 * 1024L);

        // Строим топологию
        StreamsBuilder builder = new StreamsBuilder();
        buildTopology(kafkaProperties, builder);

        // Создаем Kafka Streams
        this.streams = new KafkaStreams(builder.build(), properties);
    }

    @Override
    public void close() throws IOException {
        log.info("Завершение BlockingService");
        streams.close();
    }

    @Override
    public void run() {
        log.info("BlockingService запущен.");
        streams.start();
    }

    private void buildTopology(KafkaProperties kafkaProperties, StreamsBuilder builder) {
        // Агрегируем блокировки
        //     ключ: тот, кто блокирует
        //     значение: список тех, кого заблокировали
        String storeName = kafkaProperties.getBlockedUser().getTopic() + "-store";
        KTable<String, Set<String>> userBlocks = builder
                .<String, String>stream(kafkaProperties.getBlockedUser().getTopic())
                .groupByKey()
                .aggregate(
                        HashSet::new,
                        (userId, blockedUser, blockedUsersSet) -> {
                            blockedUsersSet.add(blockedUser);
                            return blockedUsersSet;
                        },
                        Materialized.<String, Set<String>, KeyValueStore<Bytes, byte[]>>as(storeName)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new JsonSetSerde())
                );

        // Поток личных сообщений
        KStream<String, String> messageStream = builder.stream(kafkaProperties.getPrivateMessage().getTopic());
        KStream<String, PrivateMessage> parsedMessages = messageStream
                .mapValues(value -> {
                    try {
                        return MAPPER.readValue(value, PrivateMessage.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Ошибка парсинга сообщения", e);
                    }
                });

        // Ключ = получатель (to)
        KStream<String, PrivateMessage> keyedByRecipient = parsedMessages
                .selectKey((key, msg) -> msg.getTo());

        KStream<String, Tmp> joined = keyedByRecipient
                .leftJoin(
                        userBlocks,
                        Tmp::new,
                        Joined.with(
                                Serdes.String(),
                                new PrivateMessageSerde(),
                                new JsonSetSerde()
                        )
                );

        // Фильтрация: проверяем, заблокирован ли отправитель получателем
        KStream<String, PrivateMessage> filteredMessages = joined
                .filter((key, tmp) -> {
                    log.info("key={} tmp={}", key, tmp);
                    if ((tmp == null) || (tmp.getMessage() == null)) {
                        return false;
                    }

                    PrivateMessage message = tmp.getMessage();
                    if (!StringUtils.hasText(message.getFrom())
                            || !StringUtils.hasText(message.getTo())
                            || !StringUtils.hasText(message.getText())) {
                        return false;
                    }

                    log.info("from={} to={}", message.getFrom(), message.getTo());

                    // если отправили сами себе, ничего не проверяем
                    if (message.getFrom().equals(message.getTo())) {
                        log.info("Отправили сами себе");
                        return true;
                    }

                    // Получаем список заблокированных пользователем `message.to`
                    Set<String> blockedUsersSet = tmp.getBlockedUsersSet();
                    log.info("blockedUsersSet={}", blockedUsersSet);
                    if (CollectionUtils.isEmpty(blockedUsersSet)) {
                        log.info("Юзера {} никто не заблокировал", message.getFrom());
                        return true;
                    }

                    boolean isBlocked = blockedUsersSet.contains(message.getFrom());
                    if (isBlocked) {
                        log.info("{} заблокировал(а) {}, сообщение не будет отправлено", message.getTo(), message.getFrom());
                    }

                    return !isBlocked;
                })
                .mapValues(Tmp::getMessage);


        // Отправка в выходной топик
        filteredMessages
                .mapValues(msg -> {
                    try {
                        return MAPPER.writeValueAsString(msg);
                    } catch (Exception e) {
                        throw new RuntimeException("Ошибка сериализации", e);
                    }
                })
                .to(
                        kafkaProperties.getCensoredMessage().getTopic(),
                        Produced.with(Serdes.String(), Serdes.String())
                );
    }
}
