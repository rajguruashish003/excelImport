package com.example.demo.repository;
import com.example.demo.entity.LastMatchesScores;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LastMatchScoreRepo extends CrudRepository<LastMatchesScores,Long> {

    List<LastMatchesScores> findBymatchNumer(Long matchNumer);

    @Query("Select e from LastMatchesScores e where e.player.playerName=:playerName")
    List<LastMatchesScores> getPlayerMatchHistoryByName(@Param("playerName")String playerName);
}
