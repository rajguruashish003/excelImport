package com.example.demo.controller;

import com.example.demo.ReportService;
import com.example.demo.dto.ImportDataTempDTO;
import com.example.demo.dto.LastMatchScoreDTO;
import com.example.demo.entity.LastMatchesScores;
import com.example.demo.entity.PlayersList;
import com.example.demo.repository.LastMatchScoreRepo;
import com.example.demo.service.DataImportService;
import com.example.demo.service.MatchCalculationsService;
import com.example.demo.util.Constants;
import com.example.demo.util.TagResponse;
import com.example.demo.util.Utils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.List;

@RestController
public class DemoCtrl {

    @Autowired
    MatchCalculationsService matchCalculationsService;

    @Autowired
    DataImportService dataImportService;

    @Autowired
    ReportService reportService;

    @RequestMapping(path = "/demo/hello",method = RequestMethod.GET, produces = "application/json")
    public  String DisplayInputString(@RequestParam String inputString){
        List<LastMatchScoreDTO> lastMatchScoreDTOS= matchCalculationsService.getMatchScoreDetailsByMatchNumber( Long.valueOf(inputString));
        return "Output List size is: "+lastMatchScoreDTOS.size();
    }

    @RequestMapping(path = "/demo/playermatchhistory/export",method = RequestMethod.GET,produces = "application/json")
    public ResponseEntity<Object> exportPlayerMatchHistory(@RequestParam String playerName,@RequestParam String extension, HttpServletResponse response) throws IOException {
        ResponseEntity<Object> responseEntity = null;
        TagResponse tagResponse = new TagResponse();
        List<LastMatchScoreDTO> playerMatchHistory=null;
        if(Utils.isNullOrEmpty(playerName) || Utils.isNullOrEmpty(extension)){
            tagResponse.message = Constants.MSG_FAILED;
            responseEntity = new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
        }else if(!Arrays.asList("xlsx","xls","csv").contains(extension))
        {
            tagResponse.message = "Only xlsx,xls,csv File Format Supported.";
            responseEntity = new ResponseEntity<>(tagResponse, HttpStatus.BAD_REQUEST);

        }else {
            Object exportResponse  = matchCalculationsService.exportPlayerMatchHistory(playerName,extension);
            if(exportResponse instanceof String ){
                String fileName = String.valueOf(exportResponse);
                InputStream is = new FileInputStream(fileName);
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName.split("/")[fileName.split("/").length - 1]);
                org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
                response.flushBuffer();
                return  null;
            }else if(exportResponse==null){
                tagResponse.message = "No Content to Export";
                responseEntity = new ResponseEntity<>(tagResponse, HttpStatus.OK);
            }
        }
       return responseEntity;
    }

    @RequestMapping(path = "/import/upload", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Object> uploadFile(@RequestPart("file")MultipartFile file){
        ResponseEntity<Object> responseEntity=null;
        TagResponse  tagResponse=new TagResponse();

        try {
            ImportDataTempDTO importDataTempObj =dataImportService.uploadAndValidateData(file);
            if (importDataTempObj != null) {
                responseEntity = new ResponseEntity<>(importDataTempObj, HttpStatus.OK);
            } else {
                tagResponse.message = Constants.MSG_FAILED;
                responseEntity = new ResponseEntity<>(tagResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.getStackTrace();
            tagResponse.message =e.getMessage();
            responseEntity = new ResponseEntity<>(tagResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }

    @RequestMapping(path = "/import/process", method = RequestMethod.POST,consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object>processFile(@RequestBody ImportDataTempDTO importTempData){
        ResponseEntity  responseEntity=null;
        TagResponse tagResponse=new TagResponse();
        try {
            tagResponse.message = (String) dataImportService.startImport(importTempData);
            responseEntity = new ResponseEntity<>(tagResponse, HttpStatus.OK);
        } catch (Exception e) {
            e.getStackTrace();
            tagResponse.message=e.getMessage();
            responseEntity = new ResponseEntity<>(tagResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }

    @RequestMapping(path = "/generate/report", method = RequestMethod.POST,consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object>generateReport(HttpServletResponse response){
        ResponseEntity  responseEntity=null;
        TagResponse tagResponse=new TagResponse();
        try {
            JasperPrint print = reportService.reportGeneration();
            if (print!=null) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                try {
                    JasperExportManager.exportReportToPdfStream(print, output);
                } catch (JRException e) {
                    e.printStackTrace();
                }
                byte[] outArray = output.toByteArray();
                response.setContentLength(outArray.length);
                response.setContentType("application/pdf");
                response.addHeader("Content-Disposition", "attachment; filename = document.pdf");
                OutputStream outStream;
                outStream = response.getOutputStream();
                outStream.write(outArray);
                response.flushBuffer();
            }
        } catch (Exception e) {
            e.getStackTrace();
            tagResponse.message=e.getMessage();
            responseEntity = new ResponseEntity<>(tagResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }
}
