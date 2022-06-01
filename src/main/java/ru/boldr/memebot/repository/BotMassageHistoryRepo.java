package ru.boldr.memebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.boldr.memebot.model.entity.BotMessageHistory;

import java.util.Optional;

public interface BotMassageHistoryRepo extends JpaRepository<BotMessageHistory, Long> {


    Optional<BotMessageHistory> findByMessageId(Integer messageId);


}
