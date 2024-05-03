package ru.boldr.memebot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.boldr.memebot.model.entity.FileLike

@Repository
interface FileLikeRepo : JpaRepository<FileLike, Long> {

     fun findAllByCoolFileId(coolFileId: Long): List<FileLike>
     fun findAllByMediaFileId(mediaFile: Long): List<FileLike>
     fun findAllByMediaFileIdIn(mediaFile: Collection<Long>): List<FileLike>
}
