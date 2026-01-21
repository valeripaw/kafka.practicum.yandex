package ru.valeripaw.kafka.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PrivateMessage {

    private String from;
    private String to;
    private String text;
    private long date;

}
