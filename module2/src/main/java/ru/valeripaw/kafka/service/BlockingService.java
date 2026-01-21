package ru.valeripaw.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.springframework.util.StringUtils;
import ru.valeripaw.kafka.dto.Message;
import ru.valeripaw.kafka.dto.Tmp;
import ru.valeripaw.kafka.properties.KafkaProperties;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;

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

        // GlobalKTable<String, String> хранит пары:
        //     ключ: user (тот, кто блокирует)
        //     значение: blockedUser (тот, кого заблокировали)
        String storeName = kafkaProperties.getBlockedUser().getTopic() + "-store";
        GlobalKTable<String, String> blockedUsers = builder.globalTable(
                kafkaProperties.getBlockedUser().getTopic(),
                Consumed.with(Serdes.String(), Serdes.String()),
                Materialized.as(storeName)
        );

        // Поток сообщений
        //     ключ: user, кто отправляет сообщение
        //     значение: объект Message
        KStream<String, String> privateMessagesStream = builder.stream(
                kafkaProperties.getPrivateMessage().getTopic(),
                Consumed.with(Serdes.String(), Serdes.String())
        );

        ValueMapper<String, Message> messageMapper = value -> {
            try {
                return MAPPER.readValue(value, Message.class);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка десериализации Message", e);
            }
        };
        KStream<String, Message> parsedMessages = privateMessagesStream
                .mapValues(messageMapper);

        // у blockedUsers и parsedMessages должен быть один ключ
        KStream<String, String> result = parsedMessages
                .join(
                        // Соединяем с глобальной таблицей
                        blockedUsers,
                        // Функция извлечения ключа для поиска в GlobalKTable: кому сообщение
                        (userFrom, message) -> message.getTo(),
                        // Функция соединения данных
                        Tmp::new
                )
                .filter((key, tmp) -> {
                    log.info("Обрабатываем key={} value={}", key, tmp);

                    if ((tmp == null) || (tmp.getMessage() == null)) {
                        return false;
                    }
                    Message message = tmp.getMessage();
                    if (!StringUtils.hasText(message.getFrom()) || !StringUtils.hasText(message.getTo())) {
                        return false;
                    }

                    return true;
//                    // Проверяем, заблокирован ли отправитель получателем
//                    String blockedUser = blockedUsers.value(message.getTo(), message.getFrom());
//                    // Если в хранилище есть запись: получатель (to) заблокировал отправителя (from)
//                    return blockedUser == null;
                })
                .map((key, tmp) -> KeyValue.pair(key, tmp.getMessage().getText()));

        // Отправляем результат в выходной топик
        result.to(
                kafkaProperties.getCensoredMessage().getTopic(),
                Produced.with(Serdes.String(), Serdes.String())
        );

//        // Фильтрация: проверяем, не заблокирован ли отправитель получателем
//        KStream<String, Message> filteredMessages = parsedMessages

//
//        // Отправляем отфильтрованные сообщения в выходной топик
//        filteredMessages
//                .mapValues(msg -> {
//                    try {
//                        return MAPPER.writeValueAsString(msg);
//                    } catch (Exception e) {
//                        throw new RuntimeException("Ошибка сериализации Message", e);
//                    }
//                })
//                .to("FILTERED_MESSAGES_TOPIC", Produced.with(Serdes.String(), Serdes.String()));

        // GlobalKTable<String, String> хранит пары:
        //     ключ: user (тот, кто блокирует), значение: blockedUser (тот, кого заблокировали).
        // Но для поиска нам нужно: "заблокировал ли to пользователя from?".
        // Поэтому при записи в топик user-blocks нужно отправлять ключ как user (тот, кто блокирует), а значение — blockedUser.
        // При фильтрации мы делаем:
        //     blockedUsers.value(message.to, message.from)
        // Это проверяет, есть ли в хранилище запись, где user = message.to и blockedUser = message.from.
        // GlobalKTable не поддерживает составные ключи напрямую. Поэтому мы используем поиск по ключу и значению через .value(key, value) — это работает, только если значение хранится как строка.

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
}
