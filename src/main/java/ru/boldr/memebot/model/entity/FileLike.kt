package ru.boldr.memebot.model.entity

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "file_like")
class FileLike(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @Column(name = "cool_file_id")
        val coolFileId: Long? = null,

        @Column(name = "media_file_id")
        val mediaFileId: Long? = null,

        @Column(name = "user_id")
        val userId: Long? = null,

        @Column(name = "telegram_user_id")
        val telegramUserId: String? = null,

        @Column(name = "like_time")
        val likeTime: Instant
) {

}