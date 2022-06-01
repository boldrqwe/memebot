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
@Table(name = "bot_message_history")
public class BotMessageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "chat_id")
    String chatId;

    @Column(name = "message_id")
    Integer messageId;

    @Column(name = "create_time")
    @CreationTimestamp
    LocalDateTime createTime;

    @Column(name = "update_time")
    @UpdateTimestamp
    LocalDateTime updateTime;
}
