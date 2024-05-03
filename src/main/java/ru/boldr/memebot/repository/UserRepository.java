package ru.boldr.memebot.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.boldr.memebot.model.entity.UserDetail;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserDetail, Long> {
    Optional<UserDetail> findByUsername(String username);

    List<UserDetail> findAllByIdIn(@NotNull List<Long> userIds);
}
