package com.swagger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.jsonzou.jmockdata.JMockData;
import com.github.jsonzou.jmockdata.MockConfig;
import com.github.jsonzou.jmockdata.TypeReference;
import com.swagger.utils.XmlUtil;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class RandomGenerator {

    static final String Rext = "[0-9]{1,4}";

    static final String STRING = "String";
    static final String INTEGER = "Integer";
    static final String DATETIME = "DateTime";
    static final String ARRAY = "array";
    static final String FILE = "file";

    public static String getRandomData(String rep){
        String replacement = "";
        switch (rep){
            case STRING:
                if(XmlUtil.getInstance().isEnable("//root/testDataGenerate/rules")){
                    replacement = GetRandomDataFromConfig(XmlUtil.getInstance().readSingleNodeString("//root/testDataGenerate/rules/String"));
                }else{
                    replacement = JMockData.mock(String.class);
                }
                break;
            case INTEGER:
                if(XmlUtil.getInstance().isEnable("//root/testDataGenerate/rules")){
                    replacement = GetRandomDataFromConfig(XmlUtil.getInstance().readSingleNodeString("//root/testDataGenerate/rules/Integer"));
                }else{
                    replacement = JMockData.mock(int.class).toString();
                }
                break;
            case DATETIME:
                SimpleDateFormat sDateFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                replacement = sDateFormat.format(new Date((JMockData.mock(Date.class)).toString()));
                break;
            case ARRAY:
                replacement = Arrays.asList(JMockData.mock(new TypeReference<Integer[]>(){})).toString();
                break;
            case FILE:
                replacement = "E:\\IDEA\\SwaggerGenerator\\test.xlsx";
                break;
        }
        return replacement;
    }

    public static String replaceEach(String json,String ori,String rep){
        int count = StringUtils.countMatches(json,ori);
        while(count>0){
            json = StringUtils.replaceOnce(json,ori,getRandomData(rep));
            count--;
        }
        return json;
    }

    public static String replaceSpecific(String json,String ori,String value){
        int count = StringUtils.countMatches(json,ori);
        while(count>0){
            json = StringUtils.replaceOnce(json,ori,RandomGenerator.GetRandomDataFromConfig(value));
            count--;
        }
        return json;
    }

    public static String addRandomToJson(String json,boolean withDescription) throws ParseException, DocumentException {
        if(json==null)return "";
        String pretty = "";
        json = replaceEach(json,"${RandomString}",STRING);
        json = replaceEach(json,"${Integer}",INTEGER);
        json = replaceEach(json,"${datetime}",DATETIME);
        json = replaceEach(json,"${array}",ARRAY);
        json = replaceEach(json,"${FilePath}",FILE);
        XmlUtil.getInstance().setFile("config.xml");
        if(XmlUtil.getInstance().isEnable("//root/testDataGenerate/specific")){
            for (Map.Entry<String, String> en :  XmlUtil.getInstance().getRegularKey(XmlUtil.getInstance()).entrySet()) {
                if(json.contains("${"+en.getKey()+"}")){
                    json = replaceSpecific(json,"${"+en.getKey()+"}",en.getValue());
                }
            }
        }
        if(json.contains("{")&&json.contains("}")&&!json.startsWith("[")){
            JSONObject object = JSONObject.parseObject(json);
            pretty = JSON.toJSONString(object, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteDateUseDateFormat);
        }
        return withDescription?pretty:json;
    }

    public static String GetRandomDataFromConfig(String Rex){
        return JMockData.mock(String.class,new MockConfig().stringRegex(Rex));
    }

    public static void main(String[] args) throws ParseException {
        String json = "{\"custNo\":\"${RandomString}\",\"accountNo\":\"${datetime}\",\"isOuterOrInner\":\"${datetime}\",\"pageSize\":\"${datetime}\",\"pageNum\":\"${Integer}\",\"isPage\":\"${Integer}\"}\n";
//        System.out.println(addRandomToJson(json));
        Integer[] integerArray = JMockData.mock(new TypeReference<Integer[]>(){});
        System.out.println(Arrays.asList(JMockData.mock(new TypeReference<Integer[]>(){})).toString());
//        MockConfig emailConfig = new MockConfig().stringRegex("[a-z0-9]{5,15}\\@\\w{3,5}\\.[a-z]{2,3}");
//        MockConfig zeroOneConfig = new MockConfig().stringRegex("[01]");
//        System.out.println(JMockData.mock(String.class,emailConfig));
//        System.out.println(JMockData.mock(String.class,zeroOneConfig));
        System.out.println(GetRandomDataFromConfig("[a-z0-9]{5,15}\\@\\w{3,5}\\.[a-z]{2,3}"));
        System.out.println(GetRandomDataFromConfig("[01]"));
        System.out.println(GetRandomDataFromConfig(Rext));
    }
}
