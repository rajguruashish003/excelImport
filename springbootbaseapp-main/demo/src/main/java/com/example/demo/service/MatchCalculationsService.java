package com.example.demo.service;

import com.example.demo.dto.LastMatchScoreDTO;
import com.example.demo.entity.LastMatchesScores;
import com.example.demo.entity.PlayersList;

import java.io.IOException;
import java.util.List;

public interface MatchCalculationsService {
    List<LastMatchScoreDTO> getMatchScoreDetailsByMatchNumber(long matchNumber);
    Object exportPlayerMatchHistory(String playerName,String fileExtension) throws IOException;
}
