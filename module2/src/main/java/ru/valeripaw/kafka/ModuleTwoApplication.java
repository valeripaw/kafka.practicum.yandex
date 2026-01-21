package ru.valeripaw.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import ru.valeripaw.kafka.properties.KafkaProperties;
import ru.valeripaw.kafka.service.BlockingService;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(KafkaProperties.class)
public class ModuleTwoApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ModuleTwoApplication.class, args);

        KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);

        // Создаем топики
        createTopics(kafkaProperties);

        // Создаем тестовые данные
        createTestData();

        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            try (BlockingService blockingService = new BlockingService(kafkaProperties)) {
                executorService.submit(blockingService);

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

    private static void createTestData() {
        // Properties producerProps = new Properties();
        //        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        //        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //
        //        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
        //            // Загружаем информацию о сегментах пользователей
        //            producer.send(new ProducerRecord<>(USER_SEGMENT_TOPIC, "User123", "VIP"));
        //            producer.send(new ProducerRecord<>(USER_SEGMENT_TOPIC, "User456", "Standard"));
        //            producer.send(new ProducerRecord<>(USER_SEGMENT_TOPIC, "User789", "New"));
        //
        //            // Загружаем данные о покупках
        //            producer.send(new ProducerRecord<>(PURCHASE_TOPIC, "User123", "iPhone 13, 999$"));
        //            producer.send(new ProducerRecord<>(PURCHASE_TOPIC, "User456", "Samsung Galaxy, 799$"));
        //            producer.send(new ProducerRecord<>(PURCHASE_TOPIC, "User789", "Xiaomi Redmi, 299$"));
        //
        //            producer.flush();
        //            System.out.println("Тестовые данные загружены");
        //        }
    }

}