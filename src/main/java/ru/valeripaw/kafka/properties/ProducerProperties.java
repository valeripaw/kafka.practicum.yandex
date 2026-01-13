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
     * todo
     */
    private String acks;
    /**
     * todo
     */
    private int retries;
    /**
     * todo
     */
    private long retryBackoffMs;
    /**
     * todo
     */
    private boolean enableIdempotence;
}
