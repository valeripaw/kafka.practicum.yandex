package ru.valeripaw.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import ru.valeripaw.kafka.consumer.BatchMessageConsumer;
import ru.valeripaw.kafka.consumer.SingleMessageConsumer;
import ru.valeripaw.kafka.producer.ExampleEvenProducer;
import ru.valeripaw.kafka.properties.KafkaProperties;

import java.util.UUID;
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

            int idx = 0;
            while (true) {
                String key = "key" + idx % 3;
                String msg = UUID.randomUUID().toString().substring(0, 7);
                exampleEvenProducer.sendMessage(key, msg);
                idx++;
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        }
    }

}
