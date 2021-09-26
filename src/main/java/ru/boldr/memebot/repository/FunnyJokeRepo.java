package ru.boldr.memebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.boldr.memebot.model.entity.FunnyJoke;

import java.util.List;

@Repository
public interface FunnyJokeRepo extends JpaRepository<FunnyJoke, Long> {


    List<FunnyJoke> findAllByChatId(Long chatId);
}
