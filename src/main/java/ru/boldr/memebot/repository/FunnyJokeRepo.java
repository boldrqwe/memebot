package ru.boldr.memebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.boldr.memebot.model.entity.FunnyJoke;

import java.util.List;

@Repository
public interface FunnyJokeRepo extends JpaRepository<FunnyJoke, Long> {


    List<FunnyJoke> findAllByChatId(Long chatId);

    @Query(value = "select username as username, count(*) as count " +
            "from funny_joke " +
            "where :chatId = chat_id " +
            "group by username", nativeQuery = true)
    List<JokeItem> getStats(
            @Param("chatId") Long chatId);

    interface JokeItem{

        String getUsername();

        Long getCount();


    }
}
