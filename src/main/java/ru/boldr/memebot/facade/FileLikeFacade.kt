package ru.boldr.memebot.facade

import org.springframework.stereotype.Component
import ru.boldr.memebot.model.entity.FileLike
import ru.boldr.memebot.repository.UserRepository
import ru.boldr.memebot.service.FileLikeService
import ru.boldr.memebot.util.SecurityUtil
import java.time.Instant

@Component
class FileLikeFacade(
        private val userRepository: UserRepository,
        private val fileLikeService: FileLikeService
) {
    fun addLike(mediaFileId: Long) {
        val userName = SecurityUtil.getCurrentUsername()
                ?: throw RuntimeException("не найдено имя пользователя")
        val userDetail = userRepository.findByUsername(userName)
                .orElseThrow { RuntimeException("юзер с таким именем: $userName не зарегистрирован") }

        fileLikeService.saveLike(FileLike(userId = userDetail.id, mediaFileId = mediaFileId, likeTime = Instant.now()));
    }

//    fun getAll(mediaFileIds: List<Long>): Map<Long?, List<String?>> {
//        val likes = fileLikeService.getLikesByMediaFileIdIn(mediaFileIds)
//        val mediaIdToLike = likes.groupBy { it.mediaFileId }
//        val mediaIdToUserIds = mediaIdToLike.mapValues { entry -> entry.value.map { it.userId } }
//        val userIds = mediaIdToUserIds.values.flatten().distinct();
//        val users = userRepository.findAllByIdIn(userIds)
//        val userIdToNickName = users.associateBy({ it.id }, { it.nickName })
//        return mediaIdToUserIds.mapValues { (_, userIds) ->
//            userIds.mapNotNull { userId -> userIdToNickName[userId] }
//        }
//    }

}
