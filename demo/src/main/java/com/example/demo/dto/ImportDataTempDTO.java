package com.example.demo.dto;


import java.util.ArrayList;
import java.util.Map;


public class ImportDataTempDTO{


    private String id;
    private String filePath;
    private int status;
    private String fileType;
    private ArrayList<String> lstColumn;
    private Map<String, String> filedMap;
    private String delimiter;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getFileType() {
        return fileType;
    }
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    public ArrayList<String> getLstColumn() {
        return lstColumn;
    }
    public void setLstColumn(ArrayList<String> lstColumn) {
        this.lstColumn = lstColumn;
    }

    public Map<String, String> getFiledMap() {
        return filedMap;
    }
    public void setFiledMap(Map<String, String> filedMap) {
        this.filedMap = filedMap;
    }
    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }
}
