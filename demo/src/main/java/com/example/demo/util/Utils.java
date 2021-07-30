package com.example.demo.util;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.log4j.Logger;

public class Utils {
    public static boolean isNullOrEmpty(String value)
    {
        if(value == null)
            return true;
        else return value.trim().equals("");
    }
    public static String getFileExtension(String path) throws Exception
    {
        if(path == null)
            return "";

        int lastIndexOf = path.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return path.substring(lastIndexOf);
    }
    public static String mapToStringForImport(Map<String,List<String>> mapData, String userName)
    {
        String value = "";
        List<String> listOfReport;
        StringBuilder stringBuilder = new StringBuilder();
        if(!isNullOrEmpty(userName)) {
            stringBuilder.append("Hi ").append(userName);
            stringBuilder.append("<br />");
            stringBuilder.append("Here is import data log:");
            stringBuilder.append("<br />");
        }
        if(mapData !=null)
        {
            for(String item : mapData.keySet())
            {
                stringBuilder.append("<b>").append(item).append(" :").append("</b>");
                stringBuilder.append("<br />");
                listOfReport = mapData.get(item);
                for(String itemChild : listOfReport)
                {
                    stringBuilder.append(itemChild);
                    stringBuilder.append("<br />");
                }
            }
            value = stringBuilder.toString();
        }
        return value;
    }
}
