package ru.boldr.memebot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.boldr.memebot.model.entity.BotMessageHistory;
import ru.boldr.memebot.repository.BotMassageHistoryRepo;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MassageHistoryService {

    private final BotMassageHistoryRepo botMassageHistoryRepo;

    public List<BotMessageHistory> findAll() {
        return botMassageHistoryRepo.findAll();
    }

    public void save(BotMessageHistory botMessageHistory) {
        botMassageHistoryRepo.save(botMessageHistory);
    }

    public void delete(BotMessageHistory botMessageHistory) {
        Optional<BotMessageHistory> byMessageId = botMassageHistoryRepo.findByMessageId(botMessageHistory.getMessageId());
        byMessageId.ifPresent(botMassageHistoryRepo::delete);
    }

}
