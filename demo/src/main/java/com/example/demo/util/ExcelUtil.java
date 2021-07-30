package com.example.demo.util;

import com.example.demo.exceptions.BadRequestException;
import com.google.common.base.Strings;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class ExcelUtil extends XSSFWorkbook {

    final static Logger log = Logger.getLogger(ExcelUtil.class);

    public ExcelUtil() {
        super();
    }

    public ExcelUtil(File vFile) throws IOException {
        super(new FileInputStream(vFile));
    }

    public static List<LinkedHashMap<?, ?>> readObjectsFromExcel(File file) throws Exception {
        List<LinkedHashMap<?, ?>> lstMap = new ArrayList<LinkedHashMap<?, ?>>();
        LinkedHashMap<String, String> hasmapObj = null;
        Workbook workbookHelper = null;
        workbookHelper = WorkbookFactory.create(file);
        Sheet sheet = workbookHelper.getSheetAt(0);
        try {
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                hasmapObj = new LinkedHashMap<String, String>();

                for (int j = 0; j < sheet.getRow(0).getLastCellNum(); j++) {
                    if (sheet.getRow(i) != null && sheet.getRow(i).getCell(j) != null) {
                        sheet.getRow(i).getCell(j).setCellType(CellType.STRING);
                        hasmapObj.put(sheet.getRow(0).getCell(j).getStringCellValue(), sheet.getRow(i).getCell(j).getStringCellValue());
                    }
                }
                lstMap.add(hasmapObj);
            }
        } catch (Exception ex) {
            log.error(ex);
            throw ex;
        } finally {
            if (workbookHelper != null) workbookHelper.close();
        }
        return lstMap;
    }

    public static ArrayList<String> readExcelColumnNames(File file) throws IOException, EncryptedDocumentException, InvalidFormatException {
        ArrayList<String> lstString = new ArrayList<>();
        Workbook workbookHelper = null;
        try (FileInputStream fileinput = new FileInputStream(file)) {
            workbookHelper = WorkbookFactory.create(fileinput);
            Sheet sheet = workbookHelper.getSheetAt(0);
            if(sheet.getRow(0)==null)
            {
                throw new BadRequestException(Constants.ERR_EMPTY_COLUMN_HEADER);
            }
            for (int j = 0; j < sheet.getRow(0).getLastCellNum(); j++) {
                if (sheet.getRow(0).getCell(j) == null)
                    throw new BadRequestException(Constants.ERR_EMPTY_COLUMN);
                try {
                    if (!Utils.isNullOrEmpty(sheet.getRow(0).getCell(j).getStringCellValue()))
                        lstString.add(sheet.getRow(0).getCell(j).getStringCellValue());
                }catch (Exception ex){
                    throw new BadRequestException(Constants.ERR_INVALID_COLUMN);
                }

            }
            if(!(lstString.contains("Match Number") && lstString.contains("Points") && lstString.contains("Player Id") && lstString.contains("Is Dream Team"))){
                throw new BadRequestException(Constants.ERR_INVALID_COLUMN);
            }
        } finally {
            if (workbookHelper != null) workbookHelper.close();
        }
        return lstString;
    }

    public static <T> String writeToFile(List<T> listOfPojos, List<String> columnHeaders) throws IOException {
        File file = null;
        ExcelUtil workbookHelper = null;
        Field[] fields;
        file = new File(Constants.ROOT_PATH_TEMP);
        if (!file.exists())
            file.mkdirs();
        file = new File(Constants.ROOT_PATH_TEMP + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + ".xlsx");
        try (FileOutputStream fop = new FileOutputStream(file)) {
            //	 workbookHelper = new ExcelUtil();
            SXSSFWorkbook wb = new SXSSFWorkbook(100);
            CreationHelper createHelper = wb.getCreationHelper();
            Sheet sheet = wb.createSheet("sheet1");
            Row row = sheet.createRow(0);
            for (int i = 0; i < columnHeaders.size() - 1; i++) {
                row.createCell(i).setCellValue(columnHeaders.get(i));
            }
            for (int i = 0; i < listOfPojos.size(); i++) {
                fields = listOfPojos.get(i).getClass().getDeclaredFields();
                row = sheet.createRow(i + 1);
                for (int j = 0; j < fields.length; j++) {
                    if (fields[j].getName().equals("common"))
                        continue;
                    fields[j].setAccessible(true);
                    if (fields[j].get(listOfPojos.get(i)) != null)
                        row.createCell(j).setCellValue(createHelper.createRichTextString((String) fields[j].get(listOfPojos.get(i))));
                }
            }

            wb.setCompressTempFiles(true);
            wb.write(fop);
            fop.flush();
            fop.close();
            return file.getAbsolutePath();
        } catch (Exception ex) {
            log.error(ex);
            return null;
        } finally {
            if (workbookHelper != null) workbookHelper.close();
        }
    }

    public static String writeToFileV2(List<Map<String, Object>> listOfPojos) throws IOException {
        return writeToFileV2(listOfPojos, "");
    }

    public static String writeToFileV2(List<Map<String, Object>> listOfPojos, File file) throws IOException {
//		File file = null;
//
//		file = new File(Constants.ROOT_PRIVATE_PATH_LIVE_TEMP);
//		Object[] fields = null;
        if (!file.exists())
            file.mkdirs();
//		if (fileName == null || fileName.isEmpty())
//			fileName = Utils.getAccessToken();
//		file = new File(Constants.ROOT_PRIVATE_PATH_LIVE_TEMP + File.separator + fileName + ".xlsx");
        return tryToWriteDataToExcel(listOfPojos, file);
    }

    public static String writeToFileV2(List<Map<String, Object>> listOfPojos, String fileName) throws IOException {
        File file = null;

        file = new File(Constants.ROOT_PATH_TEMP);
        Object[] fields = null;
        if (!file.exists())
            file.mkdirs();
        if (Strings.isNullOrEmpty(fileName))
            fileName = ""+LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        file = new File(Constants.ROOT_PATH_TEMP + File.separator + fileName + ".xlsx");
        return tryToWriteDataToExcel(listOfPojos, file);
    }

    private static String tryToWriteDataToExcel(List<Map<String, Object>> listOfPojos, File file) throws IOException {
        ExcelUtil workbookHelper = null;
        Object[] fields;
        try (FileOutputStream fop = new FileOutputStream(file);
             SXSSFWorkbook wb = new SXSSFWorkbook(100)
        ) {
            Sheet sheet = wb.createSheet("sheet1");
            Row row = sheet.createRow(0);
            if (listOfPojos.size() > 0) {
                fields = listOfPojos.get(0).keySet().toArray();
                for (int i = 0; i < fields.length; i++) {
                    row.createCell(i).setCellValue(fields[i].toString());
                }
                for (int i = 0; i < listOfPojos.size(); i++) {
                    row = sheet.createRow(i + 1);
                    for (int j = 0; j < fields.length; j++) {
                        if (listOfPojos.get(i).get(fields[j]) != null)
                            row.createCell(j).setCellValue(String.valueOf(listOfPojos.get(i).get(fields[j])));
                    }
                }
            }

            wb.setCompressTempFiles(true);
            wb.write(fop);
            fop.flush();
            return file.getAbsolutePath();
        } catch (Exception ex) {
            log.error(ex);
            return null;
        } finally {
            if (workbookHelper != null) workbookHelper.close();
        }
    }

    public static String writeToFileV2(List<Map<String, Object>> listOfPojos, String fileName, Map<String, String> cellFormat) throws IOException {

        List<String> currency = new ArrayList<>();
        List<String> numbers = new ArrayList<>();
        List<String> percent = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        if (cellFormat != null && cellFormat.size() > 0) {
            if (cellFormat.containsKey(Constants.CELL_FORMAT.CURRENCY.name()))
                currency.addAll(Arrays.asList(cellFormat.get(Constants.CELL_FORMAT.CURRENCY.name()).split(",")));
            if (cellFormat.containsKey(Constants.CELL_FORMAT.NUMBER.name()))
                numbers.addAll(Arrays.asList(cellFormat.get(Constants.CELL_FORMAT.NUMBER.name()).split(",")));
            if (cellFormat.containsKey(Constants.CELL_FORMAT.DATE.name()))
                dates.addAll(Arrays.asList(cellFormat.get(Constants.CELL_FORMAT.DATE.name()).split(",")));
            if (cellFormat.containsKey(Constants.CELL_FORMAT.PERCENTAGE.name()))
                percent.addAll(Arrays.asList(cellFormat.get(Constants.CELL_FORMAT.PERCENTAGE.name()).split(",")));
        }

        CellStyle currencyStyle = null;
        CellStyle numberStyle = null;
        CellStyle dateStyle = null;
        CellStyle percentStyle = null;

        File file = null;
        ExcelUtil workbookHelper = null;
        file = new File(Constants.ROOT_PATH_TEMP);
        Object[] fields = null;
        if (!file.exists())
            file.mkdirs();
        if (fileName == null || fileName.isEmpty())
            fileName = ""+LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        file = new File(Constants.ROOT_PATH_TEMP + File.separator + fileName + ".xlsx");
        try (FileOutputStream fop = new FileOutputStream(file)) {
            SXSSFWorkbook wb = new SXSSFWorkbook(100);

            if (currency.size() > 0) {
                currencyStyle = wb.createCellStyle();
                currencyStyle.setDataFormat(wb.createDataFormat().getFormat("$#,#0.00"));
            }

            if (numbers.size() > 0) {
                numberStyle = wb.createCellStyle();
                numberStyle.setDataFormat(wb.createDataFormat().getFormat("0.00"));
            }

            if (dates.size() > 0) {
                dateStyle = wb.createCellStyle();
                CreationHelper creationHelper = wb.getCreationHelper();
//				dateStyle.setDataFormat(wb.createDataFormat().getFormat("MM/dd/YY"));
                dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("MM/dd/YY"));
            }

            if (percent.size() > 0) {
                percentStyle = wb.createCellStyle();
                percentStyle.setDataFormat(wb.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(10)));
            }


            //CreationHelper createHelper = wb.getCreationHelper();
            Sheet sheet = wb.createSheet("sheet1");
            Row row = sheet.createRow(0);
            if (listOfPojos.size() > 0) {
                fields = listOfPojos.get(0).keySet().toArray();
                for (int i = 0; i < fields.length; i++) {
                    row.createCell(i).setCellValue(fields[i].toString());
                }
                Cell cell;
                for (int i = 0; i < listOfPojos.size(); i++) {
                    row = sheet.createRow(i + 1);
                    for (int j = 0; j < fields.length; j++) {
                        if (listOfPojos.get(i).get(fields[j]) != null) {
                            cell = row.createCell(j);
                            if (currency.contains(fields[j]) && !listOfPojos.get(i).get(fields[j]).toString().isEmpty()) {
                                cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(fields[j]).toString()));
                                cell.setCellStyle(currencyStyle);
                            } else if (numbers.contains(fields[j]) && !listOfPojos.get(i).get(fields[j]).toString().isEmpty()) {
                                cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(fields[j]).toString()));
                                cell.setCellStyle(numberStyle);
                            } else if (percent.contains(fields[j]) && !listOfPojos.get(i).get(fields[j]).toString().isEmpty()) {
                                cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(fields[j]).toString()) / 100);
                                cell.setCellStyle(percentStyle);
                            } else if (dates.contains(fields[j]) && !listOfPojos.get(i).get(fields[j]).toString().isEmpty()) {
                                cell.setCellValue(String.valueOf(listOfPojos.get(i).get(fields[j])));
                                cell.setCellStyle(dateStyle);
                            } else
                                cell.setCellValue(String.valueOf(listOfPojos.get(i).get(fields[j])));

                        }
                    }
                }
            }
            wb.setCompressTempFiles(true);
            wb.write(fop);
            fop.flush();
            fop.close();
            return file.getAbsolutePath();
        } catch (Exception ex) {
            log.error(ex);
            return null;
        } finally {
            if (workbookHelper != null) workbookHelper.close();
        }
    }

    public static void writeToFile(List<Map<String, Object>> listOfPojos, File file, Map<String, String> cellFormat, LinkedHashMap<String, String> entityMap, boolean readable, boolean header) throws IOException {

        List<String> currency = new ArrayList<>();
        List<String> numbers = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        List<String> percent = new ArrayList<>();
        List<String> custom = new ArrayList<>();
        List<String> decimalNumber = new ArrayList<>();
        if (cellFormat != null && cellFormat.size() > 0) {
            if (cellFormat.containsKey(Constants.CELL_FORMAT.CURRENCY.name()))
                currency = Arrays.asList(cellFormat.get(Constants.CELL_FORMAT.CURRENCY.name()).split(","));
            if (cellFormat.containsKey(Constants.CELL_FORMAT.NUMBER.name()))
                numbers = Arrays.asList(cellFormat.get(Constants.CELL_FORMAT.NUMBER.name()).split(","));
            if (cellFormat.containsKey(Constants.CELL_FORMAT.DATE.name()))
                dates = Arrays.asList(cellFormat.get(Constants.CELL_FORMAT.DATE.name()).split(","));
            if (cellFormat.containsKey(Constants.CELL_FORMAT.PERCENTAGE.name()))
                percent = Arrays.asList(cellFormat.get(Constants.CELL_FORMAT.PERCENTAGE.name()).split(","));
            if (cellFormat.containsKey(Constants.CELL_FORMAT.CUSTOM.name()))
                custom = Arrays.asList(cellFormat.get(Constants.CELL_FORMAT.CUSTOM.name()).split(","));
            if (cellFormat.containsKey(Constants.CELL_FORMAT.DECIMAL_NUMBER.name()))
                decimalNumber = Arrays.asList(cellFormat.get(Constants.CELL_FORMAT.DECIMAL_NUMBER.name()).split(","));
        }

        CellStyle currencyStyle = null;
        CellStyle numberStyle = null;
        CellStyle dateStyle = null;
        CellStyle percentStyle = null;
        CellStyle customStyle = null;
        CellStyle decimalNumberStyle = null;
        ExcelUtil workbookHelper = null;
        Object[] fields;
        if (readable) {
            try (FileOutputStream fop = new FileOutputStream(file, true)) {
                XSSFWorkbook wb;
                if (!header) {
                    wb = new XSSFWorkbook(new FileInputStream(file));
                } else {
                    wb = new XSSFWorkbook();
                }

                if (currency.size() > 0) {
                    currencyStyle = wb.createCellStyle();
                    currencyStyle.setDataFormat(wb.createDataFormat().getFormat("$#,#0.00"));
                }

                if (numbers.size() > 0) {
                    numberStyle = wb.createCellStyle();
                    numberStyle.setDataFormat(wb.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(1)));
                }

                if (dates.size() > 0) {
                    dateStyle = wb.createCellStyle();
                    CreationHelper creationHelper = wb.getCreationHelper();
                    if (!cellFormat.containsKey("DATE_FORMAT"))
                        dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("mm/dd/yyyy"));
                    else
                        dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat(cellFormat.get("DATE_FORMAT").toLowerCase()));
                }

                if (percent.size() > 0) {
                    percentStyle = wb.createCellStyle();
                    percentStyle.setDataFormat(wb.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(10)));
                }

                if (decimalNumber.size() > 0) {
                    decimalNumberStyle = wb.createCellStyle();
                    decimalNumberStyle.setDataFormat(wb.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(2)));
                }

                //CreationHelper createHelper = wb.getCreationHelper();
                XSSFSheet sheet;
                if (!header)
                    sheet = wb.getSheet("sheet1");
                else
                    sheet = wb.createSheet("sheet1");
                int rowCount = sheet.getLastRowNum();
                Row row;
                LinkedList<String> headers = new LinkedList<>(entityMap.values());
                if (listOfPojos.size() > 0) {
                    fields = listOfPojos.get(0).keySet().toArray();
                    if (header && entityMap != null && !entityMap.isEmpty()) {
                        row = sheet.createRow(+0);
                        for (int i = 0; i < headers.size(); i++) {
                            row.createCell(i).setCellValue(headers.get(i));
                        }
                    }
                    Cell cell;
                    List<String> keys = new LinkedList<>(entityMap.keySet());
                    for (int i = 0; i < listOfPojos.size(); i++) {
                        row = sheet.createRow(++rowCount);
                        for (int j = 0; j < keys.size(); j++) {
                            if (listOfPojos.get(i).get(keys.get(j)) != null) {
                                cell = row.createCell(j);
                                if (currency.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(currencyStyle);
                                } else if (numbers.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(numberStyle);
                                } else if (percent.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(keys.get(j)).toString()) / 100);
                                    cell.setCellStyle(percentStyle);
                                } else if (decimalNumber.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(decimalNumberStyle);
                                } else if (dates.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    if (!cellFormat.containsKey("DATE_FORMAT"))
                                        cell.setCellValue(new SimpleDateFormat("MM/dd/yyyy").parse(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    else
                                        cell.setCellValue(new SimpleDateFormat(cellFormat.get("DATE_FORMAT")).parse(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(dateStyle);
                                } else
                                    cell.setCellValue(StringUtils.substring(String.valueOf(listOfPojos.get(i).get(keys.get(j))), 0, 32767));
                                //cell.setCellValue(String.valueOf(listOfPojos.get(i).get(keys.get(j))));

                            }
                        }
                    }
                }
                wb.write(fop);
                fop.flush();
            } catch (Exception ex) {
                log.error(ex);
            } finally {
                if (workbookHelper != null) workbookHelper.close();
            }
        } else {
            try (FileOutputStream fop = new FileOutputStream(file)) {
                SXSSFWorkbook wb = new SXSSFWorkbook(2000);

                if (currency.size() > 0) {
                    currencyStyle = wb.createCellStyle();
                    currencyStyle.setDataFormat(wb.createDataFormat().getFormat("[$$-409]#,##0.00;-[$$-409]#,##0.00"));
                }

                if (numbers.size() > 0) {
                    numberStyle = wb.createCellStyle();
                    numberStyle.setDataFormat(wb.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(1)));
                }

                if (dates.size() > 0) {
                    dateStyle = wb.createCellStyle();
                    if (!cellFormat.containsKey("DATE_FORMAT"))
                        dateStyle.setDataFormat(wb.createDataFormat().getFormat("mm/dd/yyyy"));
                    else
                        dateStyle.setDataFormat(wb.createDataFormat().getFormat(cellFormat.get("DATE_FORMAT")));
                }

                if (custom.size() > 0) {
                    customStyle = wb.createCellStyle();
                    CreationHelper creationHelper = wb.getCreationHelper();
                    customStyle.setDataFormat(creationHelper.createDataFormat().getFormat("mm/dd/yyyy hh:mm"));
                }

                if (percent.size() > 0) {
                    percentStyle = wb.createCellStyle();
                    percentStyle.setDataFormat(wb.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(10)));
                }

                if (decimalNumber.size() > 0) {
                    decimalNumberStyle = wb.createCellStyle();
                    decimalNumberStyle.setDataFormat(wb.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(2)));
                }

                Sheet sheet = wb.createSheet("sheet1");
                Row row = sheet.createRow(0);
                LinkedList<String> headers = new LinkedList<>(entityMap.values());
                if (listOfPojos.size() > 0) {
                    fields = listOfPojos.get(0).keySet().toArray();
                    if (header && entityMap != null && !entityMap.isEmpty()) {
                        row = sheet.createRow(+0);
                        for (int i = 0; i < headers.size(); i++) {
                            row.createCell(i).setCellValue(headers.get(i));
                        }
                    }
                    Cell cell;
                    LinkedList<String> keys = new LinkedList<>(entityMap.keySet());
                    for (int i = 0; i < listOfPojos.size(); i++) {
                        row = sheet.createRow(i + 1);
                        for (int j = 0; j < keys.size(); j++) {
                            if (listOfPojos.get(i).get(keys.get(j)) != null) {
                                cell = row.createCell(j);
                                if (currency.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(currencyStyle);
                                } else if (numbers.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(numberStyle);
                                } else if (dates.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    if (!cellFormat.containsKey("DATE_FORMAT"))
                                        cell.setCellValue(new SimpleDateFormat("MM/dd/yyyy").parse(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    else
                                        cell.setCellValue(new SimpleDateFormat(cellFormat.get("DATE_FORMAT")).parse(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(dateStyle);
                                } else if (percent.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(keys.get(j)).toString()) / 100);
                                    cell.setCellStyle(percentStyle);
                                } else if (decimalNumber.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(decimalNumberStyle);
                                } else if (custom.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(new SimpleDateFormat("MM/dd/yyyy HH:mm").parse(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(customStyle);
                                } else
                                    cell.setCellValue(StringUtils.substring(String.valueOf(listOfPojos.get(i).get(keys.get(j))), 0, 32767));

                            }
                        }
                    }
                }
//				wb.setCompressTempFiles(true);
                wb.write(fop);
                fop.flush();
            } catch (Exception ex) {
                log.error(ex);
            } finally {
                if (workbookHelper != null) workbookHelper.close();
            }
        }
    }

    public static void writeToFile(List<Map<String, Object>> listOfPojos, File file, Map<String, String> cellFormat, LinkedHashMap<String, String> entityMap, boolean readable, boolean header,int startRow) throws IOException {
        List<String> currency = new ArrayList<>();
        List<String> numbers = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        List<String> percent = new ArrayList<>();
        List<String> custom = new ArrayList<>();
        if (cellFormat != null && cellFormat.size() > 0) {
            if (cellFormat.containsKey(Constants.CELL_FORMAT.CURRENCY.name()))
                currency = Arrays.asList(cellFormat.get(Constants.CELL_FORMAT.CURRENCY.name()).split(","));
            if (cellFormat.containsKey(Constants.CELL_FORMAT.NUMBER.name()))
                numbers = Arrays.asList(cellFormat.get(Constants.CELL_FORMAT.NUMBER.name()).split(","));
            if (cellFormat.containsKey(Constants.CELL_FORMAT.DATE.name()))
                dates = Arrays.asList(cellFormat.get(Constants.CELL_FORMAT.DATE.name()).split(","));
            if (cellFormat.containsKey(Constants.CELL_FORMAT.PERCENTAGE.name()))
                percent = Arrays.asList(cellFormat.get(Constants.CELL_FORMAT.PERCENTAGE.name()).split(","));
            if (cellFormat.containsKey(Constants.CELL_FORMAT.CUSTOM.name()))
                custom = Arrays.asList(cellFormat.get(Constants.CELL_FORMAT.CUSTOM.name()).split(","));
        }

        CellStyle currencyStyle = null;
        CellStyle numberStyle = null;
        CellStyle dateStyle = null;
        CellStyle percentStyle = null;
        CellStyle customStyle = null;
        ExcelUtil workbookHelper = null;
        Object[] fields;
        if (readable) {
            try (FileOutputStream fop = new FileOutputStream(file, true)) {
                XSSFWorkbook wb;
                if (!header) {
                    wb = new XSSFWorkbook(new FileInputStream(file));
                } else {
                    wb = new XSSFWorkbook();
                }

                if (currency.size() > 0) {
                    currencyStyle = wb.createCellStyle();
                    currencyStyle.setDataFormat(wb.createDataFormat().getFormat("$#,#0.00"));
                }

                if (numbers.size() > 0) {
                    numberStyle = wb.createCellStyle();
                    numberStyle.setDataFormat(wb.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(1)));
                }

                if (dates.size() > 0) {
                    dateStyle = wb.createCellStyle();
                    CreationHelper creationHelper = wb.getCreationHelper();
                    if (!cellFormat.containsKey("DATE_FORMAT"))
                        dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("mm/dd/yyyy"));
                    else
                        dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat(cellFormat.get("DATE_FORMAT").toLowerCase()));
                }

                if (percent.size() > 0) {
                    percentStyle = wb.createCellStyle();
                    percentStyle.setDataFormat(wb.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(10)));
                }

                //CreationHelper createHelper = wb.getCreationHelper();
                XSSFSheet sheet;
                if (!header)
                    sheet = wb.getSheet("sheet1");
                else
                    sheet = wb.createSheet("sheet1");
                int rowCount = sheet.getLastRowNum();
                Row row;
                LinkedList<String> headers = new LinkedList<>(entityMap.values());
                if (listOfPojos.size() > 0) {
                    fields = listOfPojos.get(0).keySet().toArray();
                    if (header && entityMap != null && !entityMap.isEmpty()) {
                        row = sheet.createRow(+0);
                        for (int i = 0; i < headers.size(); i++) {
                            row.createCell(i).setCellValue(headers.get(i));
                        }
                    }
                    Cell cell;
                    List<String> keys = new LinkedList<>(entityMap.keySet());
                    for (int i = 0; i < listOfPojos.size(); i++) {
                        row = sheet.createRow(++rowCount);
                        for (int j = 0; j < keys.size(); j++) {
                            if (listOfPojos.get(i).get(keys.get(j)) != null) {
                                cell = row.createCell(j);
                                if (currency.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(currencyStyle);
                                } else if (numbers.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(numberStyle);
                                } else if (percent.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(keys.get(j)).toString()) / 100);
                                    cell.setCellStyle(percentStyle);
                                } else if (dates.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    if (!cellFormat.containsKey("DATE_FORMAT"))
                                        cell.setCellValue(new SimpleDateFormat("MM/dd/yyyy").parse(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    else
                                        cell.setCellValue(new SimpleDateFormat(cellFormat.get("DATE_FORMAT")).parse(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(dateStyle);
                                } else
                                    cell.setCellValue(StringUtils.substring(String.valueOf(listOfPojos.get(i).get(keys.get(j))), 0, 32767));
                                //cell.setCellValue(String.valueOf(listOfPojos.get(i).get(keys.get(j))));

                            }
                        }
                    }
                }
                wb.write(fop);
                fop.flush();
            } catch (Exception ex) {
                log.error(ex);
            } finally {
                if (workbookHelper != null) workbookHelper.close();
            }
        } else {
            try (FileOutputStream fop = new FileOutputStream(file)) {
                SXSSFWorkbook wb = new SXSSFWorkbook(2000);

                if (currency.size() > 0) {
                    currencyStyle = wb.createCellStyle();
                    currencyStyle.setDataFormat(wb.createDataFormat().getFormat("[$$-409]#,##0.00;-[$$-409]#,##0.00"));
                }

                if (numbers.size() > 0) {
                    numberStyle = wb.createCellStyle();
                    numberStyle.setDataFormat(wb.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(1)));
                }

                if (dates.size() > 0) {
                    dateStyle = wb.createCellStyle();
                    if (!cellFormat.containsKey("DATE_FORMAT"))
                        dateStyle.setDataFormat(wb.createDataFormat().getFormat("mm/dd/yyyy"));
                    else
                        dateStyle.setDataFormat(wb.createDataFormat().getFormat(cellFormat.get("DATE_FORMAT")));
                }

                if (custom.size() > 0) {
                    customStyle = wb.createCellStyle();
                    CreationHelper creationHelper = wb.getCreationHelper();
                    customStyle.setDataFormat(creationHelper.createDataFormat().getFormat("mm/dd/yyyy hh:mm"));
                }

                if (percent.size() > 0) {
                    percentStyle = wb.createCellStyle();
                    percentStyle.setDataFormat(wb.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(10)));
                }

                Sheet sheet = wb.createSheet("sheet1");
                Row row;
                LinkedList<String> headers = new LinkedList<>(entityMap.values());
                if (listOfPojos.size() > 0) {
                    if (header && entityMap != null && !entityMap.isEmpty()) {
                        row = sheet.createRow(startRow);
                        for (int i = 0; i < headers.size(); i++) {
                            row.createCell(i).setCellValue(headers.get(i));
                        }
                    }
                    Cell cell;
                    LinkedList<String> keys = new LinkedList<>(entityMap.keySet());
                    for (int i = 0; i < listOfPojos.size(); i++) {
                        row = sheet.createRow(startRow+i + 1);
                        for (int j = 0; j < keys.size(); j++) {
                            if (listOfPojos.get(i).get(keys.get(j)) != null) {
                                cell = row.createCell(j);
                                if (currency.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(currencyStyle);
                                } else if (numbers.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(numberStyle);
                                } else if (dates.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    if (!cellFormat.containsKey("DATE_FORMAT"))
                                        cell.setCellValue(new SimpleDateFormat("MM/dd/yyyy").parse(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    else
                                        cell.setCellValue(new SimpleDateFormat(cellFormat.get("DATE_FORMAT")).parse(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(dateStyle);
                                } else if (percent.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(Double.valueOf(listOfPojos.get(i).get(keys.get(j)).toString()) / 100);
                                    cell.setCellStyle(percentStyle);
                                } else if (custom.contains(keys.get(j)) && !listOfPojos.get(i).get(keys.get(j)).toString().isEmpty()) {
                                    cell.setCellValue(new SimpleDateFormat("MM/dd/yyyy HH:mm").parse(listOfPojos.get(i).get(keys.get(j)).toString()));
                                    cell.setCellStyle(customStyle);
                                } else
                                    cell.setCellValue(StringUtils.substring(String.valueOf(listOfPojos.get(i).get(keys.get(j))), 0, 32767));

                            }
                        }
                    }
                }
//				wb.setCompressTempFiles(true);
                wb.write(fop);
                fop.flush();
            } catch (Exception ex) {
                log.error(ex);
            } finally {
                if (workbookHelper != null) workbookHelper.close();
            }
        }
    }

    public static void addExcelInfoOnTop(File file,Map<String,String> info){
        try (FileOutputStream fop = new FileOutputStream(file,true))
        {
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
            XSSFSheet sheet = wb.getSheet("sheet1");

            CellStyle cellStyle = wb.createCellStyle();
            XSSFFont font = wb.createFont();
            font.setBold(true);
            cellStyle.setFont(font);
            int i=0;
            Row row;
            for(Map.Entry<String,String> map : info.entrySet()){
                row = sheet.createRow(i);
                row.createCell(0).setCellValue(map.getKey());
                row.createCell(1).setCellValue(map.getValue());
                row.setRowStyle(cellStyle);
                i++;
            }
            sheet.createRow(i);

            PrintWriter pw = new PrintWriter(file);
            pw.close();
            wb.write(fop);
            fop.flush();
        }
        catch(Exception ex)
        {
            log.error("Error in Update Excel Information",ex);
        }
    }

}
