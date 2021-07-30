package com.example.demo.service;

import com.example.demo.dto.ImportDataTempDTO;
import org.springframework.web.multipart.MultipartFile;

public interface DataImportService {
    public ImportDataTempDTO uploadAndValidateData(MultipartFile multipartFile) throws Exception;

    public  Object startImport(ImportDataTempDTO importDataTempDTO) throws Exception;

}
