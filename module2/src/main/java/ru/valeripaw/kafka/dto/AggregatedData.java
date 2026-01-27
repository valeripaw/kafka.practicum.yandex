package ru.valeripaw.kafka.dto;

import lombok.*;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AggregatedData {

    private PrivateMessage message;
    private Set<String> blockedUsersSet;
}
