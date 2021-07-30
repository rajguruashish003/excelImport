package com.example.demo.component.impl;


import com.example.demo.component.BatchInsertComponent;
import com.example.demo.dto.ImportDataTempDTO;
import com.example.demo.entity.ImportDataTemp;
import com.example.demo.entity.LastMatchesScores;
import com.example.demo.entity.PlayersList;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.repository.ImportDataTempRepo;
import com.example.demo.repository.PlayersListRepo;
import com.example.demo.util.Constants;
import com.example.demo.util.CsvUtil;
import com.example.demo.util.IOUtils.ExcelToMapHandlerMF;
import com.example.demo.util.Utils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class ImportComponentImpl {
    @Autowired
    ImportDataTempRepo importDataTempRepo;

    @Autowired
    BatchInsertComponent<LastMatchesScores> lastMatchesScoresBatchInsertComponent;

    @Autowired
    PlayersListRepo playersListRepo;

    final static Logger log = Logger.getLogger(ImportComponentImpl.class);
    int rowId;
    String tempValue=null;
    Map<String,String> dataMap;
    Map<String, String> mapFile;
    List<LinkedHashMap<?, ?>> data;
    List<String> matchNumber;
    List<String>points;
    List<String>playerId;
    List<String>dreamTeam;
    LastMatchesScores lastMatchesScores;
    List<String> invalidData;

    public Map validateDataOnUpload(ImportDataTempDTO importDataTemp) throws Exception{
        synchronized (this){

            mapFile= new HashMap();
            matchNumber=new ArrayList<>();
            points=new ArrayList<>();
            playerId=new ArrayList<>();
            dreamTeam=new ArrayList<>();

            mapFile.put("matchNumber","Match Number");
            mapFile.put("points","Points");
            mapFile.put("playerId","Player Id");
            mapFile.put("dreamTeam","Is Dream Team");

            rowId = 1;

            requiredFieldValidation(mapFile);
            ImportDataTemp importDataTempFromDB = importDataTempRepo.findById(Long.parseLong(importDataTemp.getId()));

            if (importDataTemp.getFileType().equalsIgnoreCase(Constants.FILE_EXT_CSV)) {
                data = CsvUtil.readObjectsFromCsv(new File(importDataTempFromDB.getFilePath()));
                for(LinkedHashMap mapitem : data){
                    validateExcel(mapitem,mapFile);
                }
            }
            else if (importDataTemp.getFileType().equalsIgnoreCase(Constants.FILE_EXT_EXCEL)) {
                new ExcelToMapHandlerMF(importDataTempFromDB.getFilePath(), linkedHashMap -> validateExcel(linkedHashMap,mapFile));
            }

            Map map= sendImportNotification();


            if(map.get("status").toString().equalsIgnoreCase("Unsuccessful")) {
                File file = new File(importDataTempFromDB.getFilePath());
                if (file.exists())
                    file.delete();
                importDataTempRepo.delete(importDataTempFromDB);
            }

            return map;
        }

    }
    private void requiredFieldValidation(Map<String, String> mapFile) {
        if(!mapFile.containsKey("matchNumber")){
            log.info("Required Field Not Mapped");
            throw new BadRequestException(String.format(Constants.REQUIRED_FIELD_MISSING,"matchNumber"));
        }

        if(!mapFile.containsKey("points")){
            log.info("Required Field Not Mapped");
            throw new BadRequestException(String.format(Constants.REQUIRED_FIELD_MISSING,"points"));
        }

        if(!mapFile.containsKey("playerId")){
            log.info("Required Field Not Mapped");
            throw new BadRequestException(String.format(Constants.REQUIRED_FIELD_MISSING,"playerId"));
        }
    }

    public  void  validateExcel(LinkedHashMap mapitem,Map<String, String> mapFile){

        if( mapitem.get("Match Number")!=null)
            matchNumber.add((String) mapitem.get("Match Number"));

        if(mapitem.get("Points")!=null)
            points.add((String) mapitem.get("Points"));

        if(mapitem.get("Player Id")!=null)
            playerId.add((String) mapitem.get("Player Id"));

        if(mapitem.get("Dream Team")!=null)
            dreamTeam.add((String) mapitem.get("Dream Team"));



        rowId++;
        if(validate(mapitem,mapFile,rowId) && ((dataMap != null && !dataMap.isEmpty())))
        {
              //prepareData();
        }
    }

    private boolean validate(LinkedHashMap<String,String> mapitem, Map<String, String> mapFile, long rowId) {
        dataMap = new HashMap<>();
        boolean returnState=true;
        invalidData=new ArrayList<>();


        lastMatchesScores = new LastMatchesScores();
        tempValue = mapitem.get(mapFile.get("matchNumber")) == null ? null : mapitem.get(mapFile.get("matchNumber")).trim();
        if(!mapFile.containsKey("matchNumber") || (mapFile.containsKey("matchNumber") && Utils.isNullOrEmpty(tempValue))){
            invalidData.add(String.format(Constants.EMPTY_OR_NULL_DATA, rowId, "matchNumber"));
            returnState=false;
        }
        else if(tempValue != null)
            dataMap.put("matchNumber",tempValue);


        tempValue = mapitem.get(mapFile.get("points")) == null ? null : mapitem.get(mapFile.get("points")).trim();
        if(!mapFile.containsKey("points") || (mapFile.containsKey("points") && Utils.isNullOrEmpty(tempValue))){
            invalidData.add(String.format(Constants.EMPTY_OR_NULL_DATA, rowId, "points"));
            returnState=false;
        }
        else if(tempValue != null)
            dataMap.put("points",tempValue);

        tempValue = mapitem.get(mapFile.get("playerId")) == null ? null : mapitem.get(mapFile.get("playerId")).trim();
        if(!mapFile.containsKey("playerId") || (mapFile.containsKey("playerId") && Utils.isNullOrEmpty(tempValue))){
            invalidData.add(String.format(Constants.EMPTY_OR_NULL_DATA, rowId, "playerId"));
            returnState=false;
        }
        else if(tempValue != null)
            dataMap.put("playerId",tempValue);

        tempValue = mapitem.get(mapFile.get("dreamTeam")) == null ? null : mapitem.get(mapFile.get("dreamTeam")).trim();
        if(!mapFile.containsKey("dreamTeam") || (mapFile.containsKey("dreamTeam") && Utils.isNullOrEmpty(tempValue))){
            invalidData.add(String.format(Constants.EMPTY_OR_NULL_DATA, rowId, "dreamTeam"));
            returnState=false;
        }
        else if(tempValue != null)
            dataMap.put("dreamTeam",tempValue);


        return returnState;
    }
    private Map sendImportNotification() throws Exception {
        Map<String,List<String>> errorMap = new HashMap<>();
        if (invalidData.size() > 0)
            errorMap.put("Invalid Data", invalidData);


        Map importValueMap = new HashMap();

        importValueMap.put("totalRecordsToImport",""+rowId);

        if(errorMap.containsKey("Invalid Data")){
            tempValue = Utils.mapToStringForImport(errorMap,null);
            importValueMap.put("status","Unsuccessful");
            importValueMap.put("errorList",tempValue);
        } else{
            importValueMap.put("status","Successful");
        }

       /* Email email = new Email();
        EmailBody emailBody = new EmailBody();
        emailBody.setSubject(" Contact Import Log");
        emailBody.setBody(VelocityUtil.getBuildTemplate(Constant.IMPORT_TEMPLATE_PATH,importValueMap));
        email.setBody(emailBody);
        email.setTo(currentUser.getEmail());
        email.setFrom(Constant.EMAILID_FROM);
        email.setReplyTo(Constant.EMAILID_TO);
        Mail.sendEmail(email, null, Constant.FROM_NAME);*/

        return importValueMap;
    }

    public Map importData(ImportDataTempDTO importDataTempDto) throws Exception {
        synchronized (this){
            rowId = 1;

            matchNumber=new ArrayList<>();
            points=new ArrayList<>();
            playerId=new ArrayList<>();
            dreamTeam=new ArrayList<>();

            mapFile = importDataTempDto.getFiledMap();
            mapFile.put("matchNumber","Match Number");
            mapFile.put("points","Points");
            mapFile.put("playerId","Player Id");
            mapFile.put("dreamTeam","Is Dream Team");

            //if(!mapFile.containsKey(Constant.CON_ID))

            ImportDataTemp importDataTemp = importDataTempRepo.findById(Long.parseLong(importDataTempDto.getId()));
            if (importDataTemp == null || importDataTempDto.getFiledMap() == null)
                throw new BadRequestException(Constants.IMP_FILEMAP_NULL_OR_INVALID);

            lastMatchesScoresBatchInsertComponent.beingTransaction();

            if (importDataTemp.getFileType().equalsIgnoreCase(Constants.FILE_EXT_CSV)) {
                data = CsvUtil.readObjectsFromCsv(new File(importDataTemp.getFilePath()));
                for(LinkedHashMap mapitem : data){
                    importData(mapitem,mapFile);
                }
            }
            else if (importDataTemp.getFileType().equalsIgnoreCase(Constants.FILE_EXT_EXCEL)) {
                new ExcelToMapHandlerMF(importDataTemp.getFilePath(), linkedHashMap -> importData(linkedHashMap,mapFile));
            }
            lastMatchesScoresBatchInsertComponent.commitTransaction();
            Map map= sendImportNotification();
            File file = new File(importDataTemp.getFilePath());
          //  if (file.exists())
           //     file.delete();
          //  importDataTempRepo.delete(importDataTemp);
            return map;
        }
    }

    private void importData(LinkedHashMap mapitem, Map<String, String> mapFile) {
        rowId++;
        if(validate(mapitem,mapFile,rowId) && ((dataMap != null && !dataMap.isEmpty())))
        {
            prepareData();
        }
    }
    private void prepareData() {
        Iterator<String> data = dataMap.keySet().iterator();
        while (data.hasNext()){
            tempValue = data.next();
            if(tempValue.equalsIgnoreCase("matchNumber")){
                //TODO locaChapter set values to add in DB
               lastMatchesScores.setMatchNumer(Long.parseLong(dataMap.get(tempValue)));
            }else if(tempValue.equalsIgnoreCase("points")){
                lastMatchesScores.setPoints(Long.parseLong(dataMap.get(tempValue)));
            }else if(tempValue.equalsIgnoreCase("playerId")){
                Optional<PlayersList> playersList= playersListRepo.findById(Long.parseLong(dataMap.get(tempValue)));
                lastMatchesScores.setPlayer(playersList.get());
            }else if(tempValue.equalsIgnoreCase("dreamTeam")){
                lastMatchesScores.setDreamTeam(Boolean.getBoolean(dataMap.get(tempValue)));
            }
        }
        lastMatchesScoresBatchInsertComponent.submit(lastMatchesScores);
    }
}
