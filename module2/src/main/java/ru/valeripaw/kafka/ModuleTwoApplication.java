package ru.valeripaw.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import ru.valeripaw.kafka.dto.PrivateMessage;
import ru.valeripaw.kafka.properties.KafkaProperties;
import ru.valeripaw.kafka.service.BlockingService;

import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(KafkaProperties.class)
public class ModuleTwoApplication {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws JsonProcessingException {
        ApplicationContext context = SpringApplication.run(ModuleTwoApplication.class, args);

        KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);

        // Создаем топики
        createTopics(kafkaProperties);

        // Создаем тестовые данные
        createTestData(kafkaProperties);

        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            try (BlockingService blockingService = new BlockingService(kafkaProperties)) {
                executorService.submit(blockingService);

                while (true) {
                    // обрабатываем данные
                }
            } catch (Exception e) {
                log.error("{}", e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        }
    }

    private static void createTopics(KafkaProperties kafkaProperties) {
        Properties adminProps = new Properties();
        adminProps.put("bootstrap.servers", kafkaProperties.getBootstrapServers());

        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            NewTopic blockedUserTopic = new NewTopic(kafkaProperties.getBlockedUser().getTopic(), 1, (short) 1);
            NewTopic privateMessageTopic = new NewTopic(kafkaProperties.getPrivateMessage().getTopic(), 1, (short) 1);
            NewTopic censoredMessageTopic = new NewTopic(kafkaProperties.getCensoredMessage().getTopic(), 1, (short) 1);

            adminClient.createTopics(List.of(blockedUserTopic, privateMessageTopic, censoredMessageTopic)).all().get();
            log.info("Созданы топики:\n{}\n{}\n{}", kafkaProperties.getBlockedUser().getTopic(),
                    kafkaProperties.getPrivateMessage().getTopic(), kafkaProperties.getCensoredMessage().getTopic());
        } catch (Exception e) {
            log.error("Ошибка при создании топиков {}", e.getMessage(), e);
        }
    }

    private static void createTestData(KafkaProperties kafkaProperties) throws JsonProcessingException {
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
            // Загружаем информацию о заблокированых пользователях
            producer.send(new ProducerRecord<>(kafkaProperties.getBlockedUser().getTopic(),
                    "Alice", "Bob"));
            producer.send(new ProducerRecord<>(kafkaProperties.getBlockedUser().getTopic(),
                    "Alice", "Lena"));
            producer.send(new ProducerRecord<>(kafkaProperties.getBlockedUser().getTopic(),
                    "Bob", "Kate"));
            producer.send(new ProducerRecord<>(kafkaProperties.getBlockedUser().getTopic(),
                    "Bob", "Margo"));
            producer.send(new ProducerRecord<>(kafkaProperties.getBlockedUser().getTopic(),
                    "Kate", "Margo"));
            producer.send(new ProducerRecord<>(kafkaProperties.getBlockedUser().getTopic(),
                    "Kate", "Lena"));
            producer.send(new ProducerRecord<>(kafkaProperties.getBlockedUser().getTopic(),
                    "Margo", "Alice"));

            log.info("Alice заблокировала Bob и Lena\n" +
                    "Bob заблокировал Kate и Margo\n" +
                    "Kate заблокировала Margo и Lena\n" +
                    "Margo заблокировала Alice");

            List<String> users = List.of("Alice", "Bob", "Kate", "Margo", "Lena");
            String userFrom = "Alice";
            for (String userTo : users) {
                sendMessage(producer, kafkaProperties, userFrom, userTo);
            }

            userFrom = "Bob";
            for (String userTo : users) {
                sendMessage(producer, kafkaProperties, userFrom, userTo);
            }

            userFrom = "Kate";
            for (String userTo : users) {
                sendMessage(producer, kafkaProperties, userFrom, userTo);
            }

            userFrom = "Margo";
            for (String userTo : users) {
                sendMessage(producer, kafkaProperties, userFrom, userTo);
            }

            userFrom = "Lena";
            for (String userTo : users) {
                sendMessage(producer, kafkaProperties, userFrom, userTo);
            }

            producer.flush();
            log.info("Тестовые данные загружены");
        }
    }

    private static void sendMessage(KafkaProducer<String, String> producer, KafkaProperties kafkaProperties,
                                    String userFrom, String userTo) throws JsonProcessingException {
        PrivateMessage privateMessage = new PrivateMessage(
                userFrom, userTo,
                "From " + userFrom + " to " + userTo + " with love!",
                Instant.now().toEpochMilli()
        );
        String message = MAPPER.writeValueAsString(privateMessage);
        producer.send(new ProducerRecord<>(kafkaProperties.getPrivateMessage().getTopic(),
                userFrom, message));
    }

}