package ru.valeripaw.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.valeripaw.kafka.properties.KafkaProperties;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({KafkaProperties.class})
public class ModuleFourApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleFourApplication.class, args);

        while (true) {
            // обрабатываем данные
        }
    }

}