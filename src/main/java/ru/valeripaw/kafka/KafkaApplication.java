package ru.valeripaw.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import ru.valeripaw.kafka.consumer.BatchMessageConsumer;
import ru.valeripaw.kafka.consumer.SingleMessageConsumer;
import ru.valeripaw.kafka.producer.ExampleEvenProducer;
import ru.valeripaw.kafka.properties.KafkaProperties;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(KafkaApplication.class, args);

        KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);

        try (ExecutorService executorSingleMessage = Executors.newSingleThreadExecutor();
             ExecutorService executorBatchMessage = Executors.newSingleThreadExecutor()) {
            submit(executorSingleMessage, executorBatchMessage, kafkaProperties);
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        }

        System.exit(0);
    }

    private static void submit(ExecutorService executorSingleMessage, ExecutorService executorBatchMessage,
                               KafkaProperties kafkaProperties) {
        try (SingleMessageConsumer singleMessageConsumer = new SingleMessageConsumer(kafkaProperties);
             BatchMessageConsumer batchMessageConsumer = new BatchMessageConsumer(kafkaProperties);
             ExampleEvenProducer exampleEvenProducer = new ExampleEvenProducer(kafkaProperties)) {
            log.info("запустили SingleMessageConsumer");
            executorSingleMessage.submit(singleMessageConsumer);

            log.info("запустили BatchMessageConsumer");
            executorBatchMessage.submit(batchMessageConsumer);

            Scanner scanner = new Scanner(System.in);
            log.info("Введите сообщения (для выхода введите 'exit'):");

            int idx = 0;
            while (true) {
                String input = scanner.nextLine();

                if (input.equals("exit")) {
                    log.info("Выход из программы.");
                    break;
                }

                if (StringUtils.hasText(input)) {
                    String key = "key" + idx % 4;
                    exampleEvenProducer.sendMessage(key, input);
                    idx++;
                }
            }

            scanner.close();
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        }
    }

}
