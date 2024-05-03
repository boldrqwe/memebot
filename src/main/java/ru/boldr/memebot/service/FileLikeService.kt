package ru.boldr.memebot.service

import org.springframework.stereotype.Service
import ru.boldr.memebot.model.entity.FileLike
import ru.boldr.memebot.repository.FileLikeRepo

@Service
class FileLikeService(private val fileLikeRepo: FileLikeRepo) {

    fun saveLike(like: FileLike): FileLike {
        return fileLikeRepo.save(like);
    }

    fun getLikesByMediaFileId(mediaFileId: Long): List<FileLike> {
        return fileLikeRepo.findAllByMediaFileId(mediaFileId);
    }

    fun getLikesByMediaFileIdIn(mediaFileIds: List<Long>): List<FileLike> {
        return fileLikeRepo.findAllByMediaFileIdIn(mediaFileIds);
    }

    fun getLikesBy(coolFileId: Long): List<FileLike> {
        return fileLikeRepo.findAllByCoolFileId(coolFileId);
    }
}