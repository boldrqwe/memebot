package ru.boldr.memebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.boldr.memebot.model.entity.HarKachFileHistory;

import java.util.List;
import java.util.Optional;

@Repository
public interface HarkachFileHistoryRepo extends JpaRepository<HarKachFileHistory, Long> {


    Optional<HarKachFileHistory> findByChatIdAndFileName(String chatId, String filename);

    List<HarKachFileHistory> findAllByChatId(String chatId);
}
