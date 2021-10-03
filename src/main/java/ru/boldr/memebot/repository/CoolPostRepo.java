package ru.boldr.memebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.boldr.memebot.model.entity.CoolFile;

import java.util.List;

@Repository
public interface CoolPostRepo extends JpaRepository<CoolFile, Long> {

   void deleteAllByFileNameIn(List<String> fileNames);

}
