package com.api.cases;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONPathException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * author: bianjiang 2020-12-29
 */
public class GetCaseInfo {

    public static final String RES_200 = "200";

    public static final String RES_400 = "400";

    public static final String BUSINESS_CODE = "BUSINESS_CODE";

    public static final String BUSINESS_MSG = "BUSINESS_MSG";

    public static final String TEXT = "TEXT";

    public static void main(String[] args) {
    }

    /**
     * 写文件的准备操作
     * @param listReader
     * @param url
     * @param Path
     * @param mainGroupName
     * @return 要写入的文件路径
     * @throws IOException
     */
    public static String beginWriteFile(ICsvListReader listReader,String url,String Path,String mainGroupName) throws IOException {
        List<String> customerList;

        String caseFilePath = "";
        String caseMethod = "";
        String caseType = "";
        while( (customerList = listReader.read()) != null ) {
            if(url.contains(customerList.get(3))){
                caseMethod = customerList.get(3);
                caseType = customerList.get(4);
                caseFilePath =  Path+"\\"+mainGroupName+"\\"+customerList.get(0)
                        +"\\"+customerList.get(1)+"_"+customerList.get(2)+".csv";
            }
        }
        if(caseFilePath == "") {
            System.out.println("文件搜索失败");
            throw new FileNotFoundException();
        }else{
            System.out.println("准备写入文件:"+caseFilePath);
            return caseFilePath;
        }
    }

    /**
     *从响应中获取回传信息
     * @param responseCode
     * @param responseBody
     * @return 回传信息的Map
     * @throws JSONPathException
     */
    public static Map<String,String> getInfoFromResponse(String responseCode,String responseBody) throws JSONPathException {
        Map<String,String> resMap = new HashMap<>();
        Map maps = new HashMap();
        try{
            maps = (Map)JSON.parse(responseBody);
        }catch (JSONException e){
            System.out.println("响应Json解析失败");
            e.printStackTrace();
        }
        switch (responseCode){
            case RES_200:
                resMap.put(BUSINESS_CODE,maps.get("code").toString());
                resMap.put(BUSINESS_MSG,maps.get("msg").toString());
                resMap.put(TEXT,maps.get("msg").toString());
                break;
            case RES_400:
                resMap.put(BUSINESS_CODE,maps.get("status").toString());
                resMap.put(BUSINESS_MSG,maps.get("error").toString());
                resMap.put(TEXT,maps.get("message").toString().substring(0,4));
                break;
            default:
                System.out.println("非200或400的响应无法解析");
                throw new JSONException();
        }
        return resMap;
    }

    /**
     * 写入用例到csv文件
     * @param list
     * @param caseFilePath
     * @param csvPreference
     * @throws IOException
     */
    public static void writeCasesToFile(List list, String caseFilePath, CsvPreference csvPreference) throws IOException {
        File caseFile = new File(caseFilePath);
        ICsvListWriter beanWriter = null;
        //开始写文件
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(caseFile, true), StandardCharsets.UTF_8);
            beanWriter = new CsvListWriter(writer,
                    csvPreference);
            beanWriter.write(list);
            beanWriter.flush();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            beanWriter.close();
        }
        System.out.println("写入用例文件成功！");
    }

    /**
     * 对json的format
     * @param json
     * @return
     */
    public static String getJsonWithFormat(String json){
        if(json.startsWith("\"")){
            return json.substring(1,json.length()).replace("\"\"","\"");
        }else{
            return json;
        }
    }

    /**
     * jmeter使用到的最终写入用例方法
     * @param Path
     * @param mainGroupName
     * @param url
     * @param caseName
     * @param requestBody
     * @param responseCode
     * @param responseBody
     * @throws IOException
     */
    public static void writeCaseIntoFile(String Path,String mainGroupName,String url,String caseName,String requestBody,String responseCode,String responseBody) throws IOException {
        String businessCode = " ";
        String businessMsg = " ";
        String text = " ";
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        System.out.println("-----------------------写入用例信息("+format.format(new Date())+")--------------------------");
        //去掉换行符
        requestBody = requestBody.replace("\n","").replace("\r","");
        responseBody = responseBody.replace("\n","").replace("\r","").replace(";","");
        System.out.println("获取路径:"+Path+
                "\n 获取主群组:"+mainGroupName+
                "\n 获取Url:"+url+
                "\n 获取用例名:"+caseName+
                "\n 获取请求body:"+requestBody+
                "\n 获取回传Code:"+responseCode+
                "\n 获取回传Body:"+responseBody);
        Map<String,String> responseMap = getInfoFromResponse(responseCode,responseBody);
        businessCode = responseMap.get(BUSINESS_CODE);
        businessMsg = responseMap.get(BUSINESS_MSG);
        text = responseMap.get(TEXT);
        System.out.println("获取业务Code:"+businessCode+
                "\n 获取回传Msg:"+businessMsg+
                "\n 获取text:"+text);
        //读取API信息来定位需要写下用例的csv文件
        File groupfile = new File(Path+"\\"+mainGroupName+".csv");
        ICsvListReader listReader = null;
        Reader reader = new InputStreamReader(new FileInputStream(groupfile),"UTF-8");
        CsvPreference MY_PREFERENCE = (new CsvPreference.Builder('\'', 59, "\n")).build();
        listReader = new CsvListReader(reader, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
        listReader.getHeader(true); // skip the header (can't be used with CsvListReader)
        String caseFilePath = beginWriteFile(listReader,url,Path,mainGroupName);
        List list = Arrays.asList(new Object[]{caseName,
                requestBody, responseCode, businessCode, businessMsg, text});

        writeCasesToFile(list,caseFilePath,MY_PREFERENCE);
        System.out.println("-----------------------完成写入用例("+format.format(new Date())+")--------------------------");
    }
}
