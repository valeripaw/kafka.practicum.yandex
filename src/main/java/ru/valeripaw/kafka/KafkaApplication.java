package ru.valeripaw.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import ru.valeripaw.kafka.consumer.BatchMessageConsumer;
import ru.valeripaw.kafka.consumer.SingleMessageConsumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
@EnableAsync
public class KafkaApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(KafkaApplication.class, args);

        SingleMessageConsumer singleMessageConsumer = context.getBean(SingleMessageConsumer.class);
        try (ExecutorService executorSingleMessage = Executors.newSingleThreadExecutor()) {
            log.info("запустили SingleMessageConsumer");
            executorSingleMessage.submit(singleMessageConsumer::start);
        }

        BatchMessageConsumer batchMessageConsumer = context.getBean(BatchMessageConsumer.class);
        try (ExecutorService executorBatchMessage = Executors.newSingleThreadExecutor()) {
            log.info("запустили BatchMessageConsumer");
            executorBatchMessage.submit(batchMessageConsumer::start);
        }


    }

//	public static void main(String[] args) {
//		// 1. Продюсер
//		try (KafkaExampleProducer producer = new KafkaExampleProducer(bootstrapServers, topic)) {
//			// Отправим 30 сообщений для теста
//			for (int i = 0; i < 30; i++) {
//				producer.sendMessage("key-" + i, "message-" + i);
//				try {
//					Thread.sleep(100); // небольшая задержка между отправками
//				} catch (InterruptedException e) {
//					Thread.currentThread().interrupt();
//					break;
//				}
//			}
//
//			System.out.println("Все сообщения отправлены. Запуск консьюмеров...");
//
//			// Пауза, чтобы сообщения успели записаться
//			Thread.sleep(2000);
//
//			// 2. Запуск SingleMessageConsumer в отдельном потоке
//			Thread singleConsumerThread = new Thread(() -> {
//				try (SingleMessageConsumer consumer = new SingleMessageConsumer(
//						bootstrapServers, "group-single", topic)) {
//					consumer.start();
//				} catch (Exception e) {
//					System.err.println("Ошибка в SingleMessageConsumer потоке: " + e.getMessage());
//				}
//			});
//
//			// 3. Запуск BatchMessageConsumer в отдельном потоке
//			Thread batchConsumerThread = new Thread(() -> {
//				try (BatchMessageConsumer consumer = new BatchMessageConsumer(
//						bootstrapServers, "group-batch", topic)) {
//					consumer.start();
//				} catch (Exception e) {
//					System.err.println("Ошибка в BatchMessageConsumer потоке: " + e.getMessage());
//				}
//			});
//
//			// Запускаем оба консьюмера
//			singleConsumerThread.start();
//			batchConsumerThread.start();
//
//			// Ждём завершения (или можно оставить работать)
//			singleConsumerThread.join(10_000); // 10 секунд
//			batchConsumerThread.join(10_000);
//
//			// Прерываем, если ещё работают
//			singleConsumerThread.interrupt();
//			batchConsumerThread.interrupt();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

}
