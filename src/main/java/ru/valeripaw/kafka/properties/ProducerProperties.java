package ru.valeripaw.kafka.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProducerProperties {

    /**
     * Наименование топика.
     */
    private String topic;
    /**
     *
     */
    private String acks;
    /**
     *
     */
    private int retries;
    /**
     *
     */
    private boolean enableIdempotence;
}
