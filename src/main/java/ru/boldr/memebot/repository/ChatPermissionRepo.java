package ru.boldr.memebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.boldr.memebot.model.entity.ChatPermission;

import java.util.Optional;

@Repository
public interface ChatPermissionRepo extends JpaRepository<ChatPermission, Long> {

  Optional<ChatPermission> findByChatId(Long id);

}
