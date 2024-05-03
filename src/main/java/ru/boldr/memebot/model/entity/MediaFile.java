package ru.boldr.memebot.model.entity;

import lombok.*;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "media_file")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Exclude
    private Long id;

    @Transient
    private byte[] fileData;

    private String filePath;

    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;

    @Column(name = "comment")
    @Nullable
    private String comment;

    @Column(name = "tread_downloaded")
    private Boolean isTreadDownloaded = false;

    @Column
    @Nullable
    private Long parentId;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "parentId")
    List<MediaFile> childFiles = new ArrayList<>();

    @Column
    String fileUrl;


    @Column(name = "created_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Instant createdAt;

}

