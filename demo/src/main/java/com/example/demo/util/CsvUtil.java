package com.example.demo.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;


public class CsvUtil {

    final static Logger log = Logger.getLogger(CsvUtil.class);

    public static  List<LinkedHashMap<?, ?>> readObjectsFromCsv(File file) throws IOException {
        CsvSchema bootstrap = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        MappingIterator<LinkedHashMap<?, ?>> mappingIterator = csvMapper.reader(Map.class).with(bootstrap).readValues(  new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8));
        return mappingIterator.readAll();
    }

    /**
     * easy but memory consuming method,
     * @param file
     * @return
     * @throws IOException
     */
    public ArrayList<String> getCsvColumnNames(File file) throws IOException
    {
        List<LinkedHashMap<?, ?>> data = readObjectsFromCsv(file);
        ArrayList<String> lstStr = new ArrayList<String>();
        for(Map<?,?> mapitem : data)
        {
            for(Object objItem : mapitem.keySet())
            {
                lstStr.add((String) objItem);
            }
            break;
        }
        return lstStr;
    }

    /**
     * easy and comparely fast method(then getCsvColumnNames) to read csv file column
     * @param file
     * @return
     * @throws IOException
     */
    public static ArrayList<String> readCsvColumnNames(File file) throws IOException
    {
        @SuppressWarnings("resource")  //TODO
                String firstline = new BufferedReader(new FileReader(file)).readLine().trim();
        return new ArrayList<>(Arrays.asList(firstline.split(",")));
    }

    public void writeAsJson(List<Map<?, ?>> data, File file) throws IOException {
        ObjectMapper mapper = TagObjectMapperWrapper.getInstance();
        mapper.writeValue(file, data);
    }

    public String getJsonStringFromCsv(File file) throws IOException
    {
        List<LinkedHashMap<?, ?>> data = readObjectsFromCsv(file);
        ObjectMapper mapper = TagObjectMapperWrapper.getInstance();
        return mapper.writeValueAsString(data);
    }

    public static <I> String toCSV (List<I> listOfPojos,@SuppressWarnings("rawtypes") Class classObj) throws Exception
    {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(classObj).withHeader();
        String csvString = mapper.writer(schema).writeValueAsString(listOfPojos);
        return csvString;
    }

    public static <I> String toCSV (List<I> listOfPojos,TypeReference classObj) throws Exception
    {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(classObj).withHeader();
        String csvString = mapper.writer(schema).writeValueAsString(listOfPojos);
        return csvString;
    }

    public static <I> String toCSV (List<Map<String,Object>> listOfPojos) throws Exception
    {
        CsvMapper mapper = new CsvMapper();
        CsvSchema.Builder builder = CsvSchema.builder();
        if (listOfPojos==null || listOfPojos.size()==0)
            return "";

        Map<String,Object> map = listOfPojos.get(0);
        for(String key : map.keySet()){
            builder.addColumn(key);
        }
        CsvSchema schema = builder.build();
        StringBuilder csvString = new StringBuilder();
        csvString.append(mapper.writer(schema).writeValueAsString(map.keySet()));
        for(Map<String,Object> obj : listOfPojos){
            csvString.append(mapper.writer(schema).writeValueAsString(obj));
        }
        return csvString.toString();
    }

    public static <I> String writeToFile(List<I> listOfPojos,@SuppressWarnings("rawtypes") Class classObj)
    {
        File file = null;
        file = new File(Constants.ROOT_PATH_TEMP);
        if(!file.exists())
            file.mkdirs();

        file = new File(Constants.ROOT_PATH_TEMP+LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)+".csv");
        try (FileOutputStream fop = new FileOutputStream(file))
        {
            String csvStr = toCSV(listOfPojos, classObj);
            if(!file.exists())
                file.createNewFile();
            byte[] contentInBytes = csvStr.getBytes();
            fop.write(contentInBytes);
            fop.flush();
            fop.close();
            return file.getAbsolutePath();
        }
        catch(Exception ex)
        {
            log.error(ex);
            return null;
        }
    }

    public static <I> String writeToFile(List<I> listOfPojos)
    {
        File file = null;
        file = new File(Constants.ROOT_PATH_TEMP);
        if(!file.exists())
            file.mkdirs();

        file = new File(Constants.ROOT_PATH_TEMP+LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)+".csv");
        if(listOfPojos!=null && listOfPojos.size()==0)
            return file.getAbsolutePath();

        try (FileOutputStream fop = new FileOutputStream(file))
        {
            //String csvStr = toCSV(listOfPojos, new TypeReference<List<Map<String,Object>>>() {});
            String csvStr = toCSV((List<Map<String, Object>>) listOfPojos);
            if(!file.exists())
                file.createNewFile();
            byte[] contentInBytes = csvStr.getBytes();
            fop.write(contentInBytes);
            fop.flush();
            fop.close();
            return file.getAbsolutePath();
        }
        catch(Exception ex)
        {
            log.error(ex);
            return null;
        }
    }

    public static <I> String writeListMapToFile(List<Map<String,Object>> listOfPojos)
    {
        File file = null;
        file = new File(Constants.ROOT_PATH_TEMP);
        if(!file.exists())
            file.mkdirs();

        file = new File(Constants.ROOT_PATH_TEMP+LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)+".csv");
        try (FileOutputStream fop = new FileOutputStream(file))
        {
            String csvStr = toCSV(listOfPojos);
            if(!file.exists())
                file.createNewFile();
            byte[] contentInBytes = csvStr.getBytes();
            fop.write(contentInBytes);
            fop.flush();
            fop.close();
            return file.getAbsolutePath();
        }
        catch(Exception ex)
        {
            log.error(ex);
            return null;
        }
    }

    public static <I> String writeListMapToFile(List<Map<String,Object>> listOfPojos, File file)
    {
//		File file = null;
//		file = new File(Constant.ROOT_PRIVATE_PATH_LIVE_TEMP);
//		if(!file.exists())
//			file.mkdirs();
//
//		file = new File(Constant.ROOT_PRIVATE_PATH_LIVE_TEMP+Utils.getAccessToken()+".csv");
        try (FileOutputStream fop = new FileOutputStream(file))
        {
            String csvStr = toCSV(listOfPojos);
            if(!file.exists())
                file.createNewFile();
            byte[] contentInBytes = csvStr.getBytes();
            fop.write(contentInBytes);
            fop.flush();
            fop.close();
            return file.getAbsolutePath();
        }
        catch(Exception ex)
        {
            log.error(ex);
            return null;
        }
    }

    public static <I> String writeListMapToFile(List<Map<String,Object>> listOfPojos,String fileName)
    {
        File file = null;
        file = new File(Constants.ROOT_PATH_TEMP);
        if(!file.exists())
            file.mkdirs();

        if (fileName==null || fileName.isEmpty())
            fileName= ""+LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        file = new File(Constants.ROOT_PATH_TEMP+File.separator+fileName+".csv");
        try (FileOutputStream fop = new FileOutputStream(file))
        {
            String csvStr = toCSV(listOfPojos);
            if(!file.exists())
                file.createNewFile();
            byte[] contentInBytes = csvStr.getBytes();
            fop.write(contentInBytes);
            fop.flush();
            fop.close();
            return file.getAbsolutePath();
        }
        catch(Exception ex)
        {
            log.error(ex);
            return null;
        }
    }

    public static void writeListMapToFile(List<Map<String,Object>> listOfPojos,File file,Map<String,String> entityMap)
    {
        try (FileOutputStream fop = new FileOutputStream(file,true))
        {
            String csvStr = toCSVMapper(listOfPojos,entityMap);
            if(!file.exists())
                file.createNewFile();
            byte[] contentInBytes = csvStr.getBytes();
            fop.write(contentInBytes);
            fop.flush();
            fop.close();
        }
        catch(Exception ex)
        {
            log.error(ex);
        }
    }

    static CsvSchema.Builder builder;
    public static <I> String toCSVMapper (List<Map<String,Object>> listOfPojos,Map<String,String> entityMap) throws Exception
    {
        CsvMapper mapper = new CsvMapper();
        if (listOfPojos==null || listOfPojos.size()==0)
            return "";

        if(entityMap != null){
            builder = CsvSchema.builder();
            for(String key : entityMap.keySet()){
                builder.addColumn(key);
            }
        }
        CsvSchema schema = builder.build();
        StringBuilder csvString = new StringBuilder();
        LinkedHashSet<String> keys=new LinkedHashSet<String>();
        if(entityMap != null){
            entityMap.keySet().forEach(a->{
                keys.add(entityMap.get(a));
                return;
            });
            csvString.append(mapper.writer(schema).writeValueAsString(keys));
        }
        for(Map<String,Object> obj : listOfPojos){
            csvString.append(mapper.writer(schema).writeValueAsString(obj));
        }
        return csvString.toString();
    }

}
