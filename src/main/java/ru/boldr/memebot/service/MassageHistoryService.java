package ru.boldr.memebot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.boldr.memebot.model.entity.BotMassageHistory;
import ru.boldr.memebot.repository.BotMassageHistoryRepo;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MassageHistoryService {

    private final BotMassageHistoryRepo botMassageHistoryRepo;

    public List<BotMassageHistory> findAll() {
        return botMassageHistoryRepo.findAll();
    }

    public void save(BotMassageHistory botMassageHistory) {
        botMassageHistoryRepo.save(botMassageHistory);
    }

    public void delete(BotMassageHistory botMassageHistory) {
        Optional<BotMassageHistory> byMessageId = botMassageHistoryRepo.findByMessageId(botMassageHistory.getMessageId());
        byMessageId.ifPresent(botMassageHistoryRepo::delete);
    }

}
