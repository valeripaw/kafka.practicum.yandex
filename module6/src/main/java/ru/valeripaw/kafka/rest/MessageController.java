package ru.valeripaw.kafka.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.valeripaw.kafka.dto.OrderEvent;
import ru.valeripaw.kafka.producer.MessageProducer;

import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {

    private final MessageProducer producer;

    @PostMapping("/topic")
    public void sendTopic(@RequestBody OrderEvent msg) throws ExecutionException, InterruptedException {
        producer.sendToTopic(msg);
    }

}