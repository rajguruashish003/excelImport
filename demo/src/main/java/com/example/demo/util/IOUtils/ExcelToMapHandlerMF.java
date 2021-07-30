package com.example.demo.util.IOUtils;


import com.example.demo.util.Utils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.util.LinkedHashMap;


public class ExcelToMapHandlerMF {



    IExcelIoListner iExcelIoListner;

    public ExcelToMapHandlerMF(String sst1, IExcelIoListner iExcelIoListner) throws Exception
    {
        this.iExcelIoListner = iExcelIoListner;
        this.readObjectsFromExcel(new File(sst1));
    }

    public void readObjectsFromExcel(File file) throws Exception
    {
        LinkedHashMap<String, String> hasmapObj;
        Workbook workbookHelper;
        workbookHelper = WorkbookFactory.create(file);
        Sheet sheet = workbookHelper.getSheetAt(0);
        try
        {
            for(int i=1;i<=sheet.getLastRowNum();i++)
            {
                hasmapObj  = new LinkedHashMap<>();
                if(!isRowEmpty(sheet.getRow(i),sheet.getRow(0).getLastCellNum())){
                    for(int j=0;j<sheet.getRow(0).getLastCellNum();j++)
                    {
                        if(sheet.getRow(i) !=null && sheet.getRow(i).getCell(j) !=null)
                        {
                            sheet.getRow(i).getCell(j).setCellType(CellType.STRING);
                            hasmapObj.put(sheet.getRow(0).getCell(j).getStringCellValue(), sheet.getRow(i).getCell(j).getStringCellValue());

                        }
                    }
                    this.iExcelIoListner.writeObject(hasmapObj);
                }
            }
        }
        catch(Exception ex)
        {
            System.out.println(ex);
            throw ex;
        }
        finally
        {
            if(workbookHelper !=null) workbookHelper.close();
        }
    }

    private  boolean isRowEmpty(Row row,int lastColumn) {
        boolean nonBlankRowFound;
        if(row !=null && row.getLastCellNum() > 0){
            for(int j=0;j<lastColumn;j++)
            {
                if(row.getCell(j)!= null) {
                    if (!Utils.isNullOrEmpty((row.getCell(j).getCellType().name().equalsIgnoreCase(CellType.STRING.name()))?row.getCell(j).getStringCellValue():""+row.getCell(j).getNumericCellValue()))
                        return false;
                }
            }
        }
        return  true;
    }

}
