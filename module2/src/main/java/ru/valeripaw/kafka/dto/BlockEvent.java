package ru.valeripaw.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class BlockEvent {

    // тот, кто блокирует
    private String user;
    // тот, кого блокируют
    private String blockedUser;

}
