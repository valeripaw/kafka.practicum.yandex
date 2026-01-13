package ru.valeripaw.kafka.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsumerProperties {

    /**
     * Наименование топика.
     */
    private String topic;
    /**
     *
     */
    private String groupId;
    /**
     * todo
     */
    private boolean enableAutoCommit;
    /**
     * todo
     */
    private int autoCommitIntervalMs;
    /**
     * todo
     */
    private int maxPollRecords;
    /**
     * todo
     */
    private int fetchMinBytes;
    /**
     * todo
     */
    private int fetchMaxWaitMs;
    /**
     * todo
     */
    private long pollDurationMs;

}
