package com.swagger.utils;

import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVUtil {
    ICsvListWriter listWriter;

    private static class csvWriteHolder {
        private static final CSVUtil INSTANCE = new CSVUtil();
    }
    private CSVUtil(){
        listWriter = null;
    }
    public void setFile(String filePath) throws IOException {
        listWriter = new CsvListWriter(new FileWriter(filePath),
                CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
    }
    public static final CSVUtil getInstance() {
        return csvWriteHolder.INSTANCE;
    }

    public void write(List<?> list) throws IOException {
        listWriter.write(list);
        listWriter.flush();
    }

    public static void main(String[] args) throws IOException {
        CSVUtil.getInstance().setFile("全局数据.csv");
    }


}
