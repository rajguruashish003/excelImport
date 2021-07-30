package com.example.demo.service.impl;

import com.example.demo.component.impl.ImportComponentImpl;
import com.example.demo.dto.ImportDataTempDTO;
import com.example.demo.entity.ImportDataTemp;
import com.example.demo.repository.ImportDataTempRepo;
import com.example.demo.service.DataImportService;
import com.example.demo.util.Constants;
import com.example.demo.util.CsvUtil;
import com.example.demo.util.ExcelUtil;
import com.example.demo.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;

@Service
public class DataImportServiceImpl implements DataImportService {
    @Autowired
    ImportDataTempRepo importDataTempRepo;

    @Autowired
    ImportComponentImpl importComponent;

    @Override
    public ImportDataTempDTO uploadAndValidateData(MultipartFile multipartFile) throws Exception {
        BufferedOutputStream stream = null;
        try {
            ArrayList<String> lstColumn = null;
            ImportDataTempDTO importDataDto = null;
            ImportDataTemp importDataTemp = new ImportDataTemp();
            File baseFolder = new File(Constants.ROOT_PATH_TEMP);
            String uuid = "uuid_" + java.util.UUID.randomUUID();
            String extension = Utils.getFileExtension(multipartFile.getOriginalFilename());
            String name = Constants.ROOT_PATH_TEMP + "/" + uuid + multipartFile.getOriginalFilename();
            if (!baseFolder.exists())
                baseFolder.mkdirs();

            File fileName = new File(name);
            byte[] bytes = multipartFile.getBytes();
            stream = new BufferedOutputStream(new FileOutputStream(fileName));
            stream.write(bytes);
            stream.close();

            importDataTemp.setFilePath(name);
            importDataTemp.setStatus(0);
            if (extension != null && extension.toUpperCase().contains(Constants.FILE_EXT_CSV.toUpperCase())) {
                importDataTemp.setFileType(Constants.FILE_EXT_CSV);
                lstColumn = CsvUtil.readCsvColumnNames(new File(name));
            } else {
                importDataTemp.setFileType(Constants.FILE_EXT_EXCEL);
                lstColumn = ExcelUtil.readExcelColumnNames(new File(name));
            }

            importDataTemp = importDataTempRepo.save(importDataTemp);

            importDataDto = new ImportDataTempDTO();
            importDataDto.setId(String.valueOf(importDataTemp.getId()));
            importDataDto.setFileType(importDataTemp.getFileType());
            importDataDto.setLstColumn(lstColumn);

            Map map = importComponent.validateDataOnUpload(importDataDto);
            importDataDto.setFiledMap(map);

            return importDataDto;
        } finally {
            if (stream != null) stream.close();
        }
    }

    @Override
    public Object startImport(ImportDataTempDTO importDataTempDTO) throws Exception {

        Object result=importComponent.importData(importDataTempDTO);
        if(result!=null)
            return "success";
        else
            return "faild";
    }
}
