package com.example.demo.repository;

import com.example.demo.entity.ImportDataTemp;
import org.springframework.data.repository.CrudRepository;

public interface ImportDataTempRepo extends CrudRepository<ImportDataTemp,Long>{

    ImportDataTemp findById(long id);

}
