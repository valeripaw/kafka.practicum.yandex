package ru.valeripaw.kafka.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.valeripaw.kafka.producer.MessageProducer;

import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {

    private final MessageProducer producer;

    @PostMapping("/topic1")
    public void sendTopic1(@RequestBody String msg) throws ExecutionException, InterruptedException {
        producer.sendToTopic1(msg);
    }

    @PostMapping("/topic2")
    public void sendTopic2(@RequestBody String msg) throws ExecutionException, InterruptedException {
        producer.sendToTopic2(msg);
    }

}