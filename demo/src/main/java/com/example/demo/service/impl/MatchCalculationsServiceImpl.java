package com.example.demo.service.impl;

import com.example.demo.dto.LastMatchScoreDTO;
import com.example.demo.entity.LastMatchesScores;
import com.example.demo.entity.PlayersList;
import com.example.demo.repository.LastMatchScoreRepo;
import com.example.demo.repository.PlayersListRepo;
import com.example.demo.service.MatchCalculationsService;
import com.example.demo.util.Constants;
import com.example.demo.util.CsvUtil;
import com.example.demo.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class MatchCalculationsServiceImpl implements MatchCalculationsService {

    @Autowired
    LastMatchScoreRepo lastMatchScoreRepo;

    @Autowired
    PlayersListRepo playersListRepo;

    @Override
    public List<LastMatchScoreDTO>getMatchScoreDetailsByMatchNumber(long matchNumber){
        List<LastMatchScoreDTO> lastMatchScoreDTOS=new ArrayList<>();
        List<LastMatchesScores> lastMatchesScores= lastMatchScoreRepo.findBymatchNumer(matchNumber);
        for(LastMatchesScores obj:lastMatchesScores){
            lastMatchScoreDTOS.add(obj.toDto());
        }
        return lastMatchScoreDTOS;
    }

    @Override
    public Object exportPlayerMatchHistory(String playerName, String fileExtension) throws IOException {

        List<Map<String,Object>> mapList = new ArrayList<>();
        Map<String,Object> map;
        List<LastMatchesScores> playerMatchHistory= lastMatchScoreRepo.getPlayerMatchHistoryByName(playerName);
        for(LastMatchesScores obj:playerMatchHistory){
            map = new HashMap<>();
            map.put("matchNumber",obj.getMatchNumer());
            map.put("points",obj.getPoints());
            map.put("playerId",obj.getPlayer().getId());
            map.put("dreamTeam",obj.isDreamTeam());
            mapList.add(map);
        }

        File file = new File(Constants.ROOT_PATH_TEMP);

        LinkedHashMap<String, String> entityMap= new LinkedHashMap<>();
        entityMap.put("matchNumber","Match Number");
        entityMap.put("points","Points");
        entityMap.put("playerId","Player Id");
        entityMap.put("dreamTeam","Is Dream Team");


        String outFilePath=null;
        if(mapList == null || mapList.size() == 0){
            return null;
        }
        if (!file.exists())
            file.mkdirs();

        if (fileExtension != null && fileExtension.equalsIgnoreCase(Constants.FILE_EXT_CSV)) {
            outFilePath = CsvUtil.writeListMapToFile(mapList);
        }else {
            outFilePath = ExcelUtil.writeToFileV2(mapList);
        }

        file = createFile(mapList,fileExtension,entityMap);

        return file.getAbsolutePath();
    }

    private File createFile(List<Map<String, Object>> finalList, String extension, LinkedHashMap<String, String> entityMap) throws IOException {
        Map<String,String> cellFramt=new HashMap<>();

        File file;
        if(extension.equals(Constants.FILE_EXT_CSV)){
            file = new File(Constants.ROOT_PATH_TEMP+File.separator+"playersMatchHistory"+System.currentTimeMillis()+".csv");
        }else{
            file = new File(Constants.ROOT_PATH_TEMP+File.separator+"playersMatchHistory"+System.currentTimeMillis()+".xlsx");
        }

        if(extension.equals(Constants.FILE_EXT_CSV))
            CsvUtil.writeListMapToFile(finalList,file,entityMap);
        else
            ExcelUtil.writeToFile(finalList,file,cellFramt,entityMap,false,true);

        return file;
    }


}
