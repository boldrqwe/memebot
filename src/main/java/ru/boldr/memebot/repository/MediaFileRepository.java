package ru.boldr.memebot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.boldr.memebot.model.entity.MediaFile;

import java.util.Collection;
import java.util.List;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
    // В интерфейсе MediaFileRepository добавьте метод для получения страницы медиа-файлов с данными файла с использованием Pageable


    Page<MediaFile> findAll(Pageable pageable);

    Page<MediaFile> findAllByCommentNotNullOrderByCreatedAtDesc(Pageable pageable);

    @Query(value = """ 
                        select id as Id,
                        file_url as fileUrl
                        from media_file       
            """, nativeQuery = true)
    List<FileUrl> findAllWithoutData();


    List<MediaFile> findAllByFileUrlIn(Collection<String> fileUrls);

    List<MediaFile> findAllByFileUrl(String message);

    Collection<MediaFile> findAllByParentIdIn(List<Long> parentIds);
}
