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
@Table(name = "cool_file")
public class CoolFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "file_name")
    String fileName;

    @Column(name = "create_time")
    @CreationTimestamp
    LocalDateTime createTime;

    @Column(name = "update_time")
    @UpdateTimestamp
    LocalDateTime updateTime;
}
