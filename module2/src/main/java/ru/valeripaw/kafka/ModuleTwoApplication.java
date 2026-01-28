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
import ru.valeripaw.kafka.properties.BannedWordsProperties;
import ru.valeripaw.kafka.properties.KafkaProperties;
import ru.valeripaw.kafka.service.BlockingService;

import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({KafkaProperties.class, BannedWordsProperties.class})
public class ModuleTwoApplication {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Random RANDOM = new Random();

    public static void main(String[] args) throws JsonProcessingException {
        ApplicationContext context = SpringApplication.run(ModuleTwoApplication.class, args);

        KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);
        BannedWordsProperties bannedWordsProperties = context.getBean(BannedWordsProperties.class);

        // Создаем топики
        createTopics(kafkaProperties);

        // Создаем тестовые данные
        createTestData(kafkaProperties);

        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            try (BlockingService blockingService = new BlockingService(kafkaProperties, bannedWordsProperties)) {
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
            blockedUsers(producer, kafkaProperties);
            sendMessages(producer, kafkaProperties);

            producer.flush();
            log.info("Тестовые данные загружены");
        }
    }

    private static void blockedUsers(KafkaProducer<String, String> producer, KafkaProperties kafkaProperties) {
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
    }


    private static void sendMessages(KafkaProducer<String, String> producer, KafkaProperties kafkaProperties)
            throws JsonProcessingException {
        List<String> users = List.of("Alice", "Bob", "Kate", "Margo", "Lena");

        List<String> sentences = List.of(
                "Голубь сидел на подоконнике и грелся на солнце",
                "Голуби кружили над площадью в поисках еды",
                "Я увидел голубя с повреждённым крылом",
                "В парке было много голубей, привыкших к людям",
                "Я насыпал крошек голубю, который подошёл ближе всех",
                "Дети радовались, когда бросали зёрна голубям",
                "Художник рисовал голубя на старой открытке",
                "Шум голубей разбудил меня ранним утром",
                "Он любовался голубем, спокойно шагающим по площади",
                "Мы наблюдали за голубями, сидящими на крыше",
                "В рассказе говорилось о голубе, который всегда возвращался домой",
                "Экскурсовод рассказывал о голубях, живущих в этом районе"
        );

        String userFrom = "Alice";
        for (String userTo : users) {
            sendMessage(producer, kafkaProperties, userFrom, userTo, sentences);
        }

        userFrom = "Bob";
        for (String userTo : users) {
            sendMessage(producer, kafkaProperties, userFrom, userTo, sentences);
        }

        userFrom = "Kate";
        for (String userTo : users) {
            sendMessage(producer, kafkaProperties, userFrom, userTo, sentences);
        }

        userFrom = "Margo";
        for (String userTo : users) {
            sendMessage(producer, kafkaProperties, userFrom, userTo, sentences);
        }

        userFrom = "Lena";
        for (String userTo : users) {
            sendMessage(producer, kafkaProperties, userFrom, userTo, sentences);
        }
    }

    private static void sendMessage(KafkaProducer<String, String> producer, KafkaProperties kafkaProperties,
                                    String userFrom, String userTo, List<String> sentences) throws JsonProcessingException {
        PrivateMessage privateMessage = new PrivateMessage(
                userFrom, userTo,
                "From " + userFrom + " to " + userTo + " with love!",
                Instant.now().toEpochMilli()
        );
        String message = MAPPER.writeValueAsString(privateMessage);
        producer.send(new ProducerRecord<>(kafkaProperties.getPrivateMessage().getTopic(),
                userFrom, message));

        privateMessage.setText(sentences.get(randomIdx(sentences.size())));
        message = MAPPER.writeValueAsString(privateMessage);
        producer.send(new ProducerRecord<>(kafkaProperties.getPrivateMessage().getTopic(),
                userFrom, message));
    }

    private static int randomIdx(int max) {
        return RANDOM.nextInt(max);
    }

}