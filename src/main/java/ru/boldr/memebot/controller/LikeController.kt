package ru.boldr.memebot.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.boldr.memebot.facade.FileLikeFacade


@RestController
@RequestMapping("/likes")
class LikeController(private val fileLikeFacade: FileLikeFacade) {

    @PostMapping("/add")
    fun addLike(@RequestBody mediaFileId: Long) {
        fileLikeFacade.addLike(mediaFileId)
    }

//    @GetMapping
//    fun getAll(mediaFileIds: List<Long>): Map<Long?, List<String?>> {
//       return fileLikeFacade.getAll(mediaFileIds)
//    }
}