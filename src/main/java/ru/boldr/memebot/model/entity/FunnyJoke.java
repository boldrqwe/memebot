package ru.boldr.memebot.model.entity;


import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
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

    @Column(name = "create_time")
    @CreationTimestamp
    LocalDateTime createTime;

    @Column(name = "update_time")
    @UpdateTimestamp
    LocalDateTime updateTime;
}
