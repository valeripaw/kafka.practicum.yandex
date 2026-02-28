package ru.valeripaw.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.valeripaw.kafka.properties.KafkaProperties;

@SpringBootApplication
@EnableConfigurationProperties({KafkaProperties.class})
public class ModuleFiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleFiveApplication.class, args);
    }

}