package ru.boldr.memebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.boldr.memebot.model.entity.BotMassageHistory;

import java.util.Optional;

public interface BotMassageHistoryRepo extends JpaRepository<BotMassageHistory, Long> {


    Optional<BotMassageHistory> findByMessageId(Integer messageId);
}
