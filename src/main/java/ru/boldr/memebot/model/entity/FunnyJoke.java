package ru.boldr.memebot.model.entity;


import lombok.*;

import javax.persistence.*;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@ToString
@Table(name = "funny_joke")
public class FunnyJoke {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "message_id")
    Integer messageId;

    @Column(name = "user_id")
    Integer userId;

    @Column(name = "username")
    String username;

    @Column(name = "text")
    String text;

    @Column(name = "chatId")
    Long chatId;
}
