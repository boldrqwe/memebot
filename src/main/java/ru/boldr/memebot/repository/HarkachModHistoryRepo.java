package ru.boldr.memebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.boldr.memebot.model.entity.HarkachModHistory;

@Repository
public interface HarkachModHistoryRepo extends JpaRepository<HarkachModHistory, Long> {
    void deleteByChatId(String chatId);
}
