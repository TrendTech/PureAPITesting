package com.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.swagger.beans.APIParameter;
import com.swagger.beans.GroupBeans;
import com.swagger.utils.XmlUtil;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Type;
import java.util.*;

import static io.restassured.RestAssured.given;

public class SwaggerParser {

    public static String getSchemaStringFromPath(String path){
        return path.substring(path.lastIndexOf("/")+1,path.length());
    }

//    private void ifArrayRecursion(Map<String, Object> itValue, Map<String, Map<String, Object>> definitionMap,
//                                  String dataFormat, Map<String, Object> myHashMapPre,
//                                  Map.Entry<String, Object> entryPreIt, Integer initNums) {
//        if (initNums < RECURSION_NUMS) {
//            Map<String, Object> newHashMap = new HashMap<>(128);
//            ArrayList<Map<String, Object>> newArrayListMap1 = new ArrayList<>();
//            ArrayList newArrayListMap2 = new ArrayList();
//            //如果为array类型，说明可能还嵌套一层的数据
//            Map<String, Object> items = (Map<String, Object>) itValue.get("items");
//            if (items.get(REF_VAL) != null || itValue.get(REF_VAL) != null) {
//                //获取到items中$ref的值
//                String itemsRef = (String) items.get("$ref");
//                Object itemsRefs;
//                itemsRefs = (itemsRef != null) ? itemsRef : itValue.get(REF_VAL);
//                //通过definitionMap获取到itemsRef对应的definitions
//                Map<String, Object> itemsRefMap = definitionMap.get(itemsRefs);
//                //继续获取properties中的数据
//                Map<String, Object> itemsPropertiesMap = (Map<String, Object>) itemsRefMap.get("properties");
//                //声明迭代器
//                if (itemsPropertiesMap != null) {
//                    Iterator<Map.Entry<String, Object>> itemsIterator = itemsPropertiesMap.entrySet().iterator();
//                    while (itemsIterator.hasNext()) {
//                        //拿到具体对象
//                        Map.Entry<String, Object> itemsIt = itemsIterator.next();
//                        Map<String, Object> itemsItValue = (Map<String, Object>) itemsIt.getValue();
//                        //取到type来为后续类型做判断
//                        String itemsType = (String) itemsItValue.get("type");
//                        if (!StringUtils.isBlank(itemsType)) {
//                            switch (itemsType) {
//                                case "string":
//                                    if (itemsItValue.get("format") != null && !"".equals(itemsItValue.get("format"))) {
//                                        String itemsFormat = (String) itemsItValue.get("format");
//                                        if ("date-time".equals(itemsFormat) || "dateTime".equals(itemsFormat)) {
//                                            newHashMap.put(itemsIt.getKey(), dataFormat);
//                                        }
//                                    } else {
//                                        newHashMap.put(itemsIt.getKey(), "string");
//                                    }
//                                    break;
//                                case "integer":
//                                    newHashMap.put(itemsIt.getKey(), 0);
//                                    break;
//                                case "number":
//                                    newHashMap.put(itemsIt.getKey(), 0.0);
//                                    break;
//                                case "boolean":
//                                    newHashMap.put(itemsIt.getKey(), true);
//                                    break;
//                                case "array":
//                                    Integer nums = initNums + 1;
//                                    ifArrayRecursion(itemsItValue, definitionMap, dataFormat, newHashMap, itemsIt, nums);
//                                default:
//                                    newHashMap.put(itemsIt.getKey(), new Object());
//                                    break;
//                            }
//                        }
//                    }
//                }
//                newArrayListMap1.add(newHashMap);
//                myHashMapPre.put(entryPreIt.getKey(), newArrayListMap1);
//            } else {
//                //没有ref的也要去解析array
//                String typeArray = (String) items.get("type");
//                switch (typeArray) {
//                    case "string":
//                        newArrayListMap2.add("string");
//                        break;
//                    case "integer":
//                        newArrayListMap2.add(0);
//                        break;
//                    case "number":
//                        newArrayListMap2.add(0.0);
//                        break;
//                    case "boolean":
//                        newArrayListMap2.add(true);
//                        break;
//                    default:
//                        newArrayListMap2.add(new Object());
//                        break;
//                }
//                myHashMapPre.put(entryPreIt.getKey(), newArrayListMap2);
//            }
//        } else {
//            System.out.println("当前对象递归次数超出！！！");
//        }
//    }

    public static APIParameter parseParameterIn(Map<String, Object> map4){
        APIParameter apiParameter = new APIParameter();
        for(Map.Entry<String, Object> entry4 : map4.entrySet()) {
            if(entry4.getKey().equals("in")){
                apiParameter.setInValue(entry4.getValue().toString());
            }
        }
        if(apiParameter.getInValue()==null) apiParameter.setInValue("");
        for(Map.Entry<String, Object> entry4 : map4.entrySet()) {
            if(apiParameter.getInValue().equals("path")){
                if(entry4.getKey().equals("type")){
                    apiParameter.setInType(entry4.getValue().toString());
                }
            }else if((apiParameter.getInValue().equals("query"))||(apiParameter.getInValue().equals("formData"))){
                if(entry4.getKey().equals("type")){
                    apiParameter.setInType(entry4.getValue().toString());
                }
                if(entry4.getKey().equals("name")){
                    apiParameter.setInName(entry4.getValue().toString());
                }
            }else if(apiParameter.getInValue().equals("body")){
                if(entry4.getKey().equals("schema")){
                    Map<String,Object> map5 = (Map<String, Object>) entry4.getValue();
                    for(Map.Entry<String,Object> entry5 : map5.entrySet()) {
                        if(entry5.getKey().equals("originalRef")){
                            apiParameter.setSchema_Ref(entry5.getValue().toString());
                        }else{
                            if(entry5.getKey().equals("type")){
                                apiParameter.setSchema_type(entry5.getValue().toString());
                            }
                            if(entry5.getKey().equals("items")){
                                Map<String,Object> map6 = (Map<String, Object>) entry5.getValue();
                                for(Map.Entry<String,Object> entry6 : map6.entrySet()) {
                                    if(entry6.getKey().equals("type")){
                                        apiParameter.setSchema_array_type(entry6.getValue().toString());
                                    }else if(entry6.getKey().equals("originalRef")){
                                        apiParameter.setSchema_Ref(entry6.getValue().toString());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return apiParameter;
    }

    public static OpenAPI getOpenAPIFromResponse(Response res){
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);
        SwaggerParseResult result = new OpenAPIParser().readContents(res.getBody().asString(), null, options);
        return result.getOpenAPI();
    }

    public static String getSchemafromMap(Map<String,Object> map) throws JsonProcessingException {
        return StringEscapeUtils.unescapeJava(new ObjectMapper().writeValueAsString(map));
    }

    public static Map<String,Object> parseSchemaToJson(OpenAPI openAPI,String schema,Map<String,Object> jsonMap,boolean withDescription) throws JsonProcessingException, DocumentException {
        Schema schemas = openAPI.getComponents().getSchemas().get(schema);
        Iterator<Map.Entry<String, Schema>> itr = schemas.getProperties().entrySet().iterator();
        while(itr.hasNext()){
            Map.Entry<String, Schema> entry = itr.next();
            if((entry.getValue().getType()==null)&&(entry.getValue().get$ref()!=null)){
//                sBuffer.append("\'data\':"+parseSchemaToJson(openAPI, getSchemaStringFromPath(entry.getValue().get$ref()),jsonMap)+"}");
                jsonMap.put(entry.getKey(), (Map<String,Object>)parseSchemaToJson(openAPI, getSchemaStringFromPath(entry.getValue().get$ref()),new HashMap<String,Object>(),true));
//                System.out.println(jsonMap.toString());
            }else{
                String saveKey = entry.getKey();
                if(SwaggerGenerator.filterName(entry.getValue().getDescription())!=""){
                    saveKey = entry.getKey()+"$("+SwaggerGenerator.filterName(entry.getValue().getDescription())+")";
                }
                String key = withDescription?saveKey:entry.getKey();
                switch (entry.getValue().getType()) {
                    case "string":
                        if(entry.getValue().getFormat()=="date-time"){
//                            sBuffer.append(entry.getKey()+":\"${datatime}\"");
                            jsonMap.put(key,"${datetime}");
                        }else{
                            jsonMap.put(key, "${RandomString}");
                        }
                        break;
                    case "integer":
                        jsonMap.put(key, "${Integer}");
//                        sBuffer.append(entry.getKey()+":\"${RandomInteger}\"");
                        break;
                    case "array":

                        break;
                    case "boolean":
                        jsonMap.put(key, true);
                        break;
                }
                if(XmlUtil.getInstance().isEnable("//root/testDataGenerate/specific")){
                    XmlUtil.getInstance().getRegularKey(XmlUtil.getInstance()).entrySet().forEach(f ->{
                        if(f.getKey().equals(entry.getKey())){
                            jsonMap.entrySet().forEach(e ->{
                                if(e.getKey().contains(f.getKey())){
                                    e.setValue("${"+entry.getKey()+"}");
                                }
                            });
//                            jsonMap.put(entry.getKey(),"${"+entry.getKey()+"}");
                        }
                    });
                }
            }
        }
        return jsonMap;
//        System.out.println(jsonMap.toString());
//        return StringEscapeUtils.unescapeJava(new ObjectMapper().writeValueAsString(jsonMap));
    }
//
//    public static String parseSchemaToJson(OpenAPI openAPI,String schema,Map<String,Object> jsonMap,boolean withDescription) throws JsonProcessingException, DocumentException {
//        Schema schemas = openAPI.getComponents().getSchemas().get(schema);
//        Iterator<Map.Entry<String, Schema>> itr = schemas.getProperties().entrySet().iterator();
//        while(itr.hasNext()){
//            Map.Entry<String, Schema> entry = itr.next();
//            if((entry.getValue().getType()==null)&&(entry.getValue().get$ref()!=null)){
////                sBuffer.append("\'data\':"+parseSchemaToJson(openAPI, getSchemaStringFromPath(entry.getValue().get$ref()),jsonMap)+"}");
//                jsonMap.put(entry.getKey(), parseSchemaToJson(openAPI, getSchemaStringFromPath(entry.getValue().get$ref()),new HashMap<String,Object>(),false));
////                System.out.println(jsonMap.toString());
//            }else{
//                String saveKey = entry.getKey();
//                if(SwaggerGenerator.filterName(entry.getValue().getDescription())!=""){
//                    saveKey = entry.getKey()+"$("+SwaggerGenerator.filterName(entry.getValue().getDescription())+")";
//                }
//                String key = withDescription?saveKey:entry.getKey();
//                switch (entry.getValue().getType()) {
//                    case "string":
//                        if(entry.getValue().getFormat()=="date-time"){
////                            sBuffer.append(entry.getKey()+":\"${datatime}\"");
//                            jsonMap.put(key,"${datetime}");
//                        }else{
//                            jsonMap.put(key, "${RandomString}");
//                        }
//                        break;
//                    case "integer":
//                        jsonMap.put(key, "${Integer}");
////                        sBuffer.append(entry.getKey()+":\"${RandomInteger}\"");
//                        break;
//                    case "array":
//
//                        break;
//                    case "boolean":
//                        jsonMap.put(key, true);
//                        break;
//                }
//                if(XmlUtil.getInstance().isEnable("//root/testDataGenerate/specific")){
//                    XmlUtil.getInstance().getRegularKey(XmlUtil.getInstance()).entrySet().forEach(f ->{
//                        if(f.getKey().equals(entry.getKey())){
//                            jsonMap.put(entry.getKey(),"${"+entry.getKey()+"}");
//                        }
//                    });
//                }
//            }
//        }
////        System.out.println(jsonMap.toString());
//        return StringEscapeUtils.unescapeJava(new ObjectMapper().writeValueAsString(jsonMap));
//    }

    public static void main(String[] args) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read("config.xml");
//        document.selectSingleNode("//root/swagger/ServerUrl").getText();
        final String TestCasePath = document.selectSingleNode("//root/global/casePath").getText();;
        RestAssured.baseURI = document.selectSingleNode("//root/swagger/ServerUrl").getText();
        RestAssured.port = Integer.parseInt(document.selectSingleNode("//root/swagger/ServerPort").getText());
        Response groupResponse = given().header("Content-Type", "application/json").
                get(document.selectSingleNode("//root/swagger/Path").getText()).then().extract().response();
        Gson gson = new Gson();
        Type type = new TypeToken<List<GroupBeans>>() {}.getType();
        List<GroupBeans> groupList = gson.fromJson(groupResponse.getBody().print(), type);
        groupList.forEach(x -> {
            String mainGroupName = x.getName().substring(0,x.getName().indexOf("("));
            String mainGroupFilePath = TestCasePath+"\\"+mainGroupName+".csv";
            Response APIResponse = given().header("Content-Type", "application/json").
                    get("/bems"+x.getLocation()).then().extract().response();
            OpenAPI openAPI = getOpenAPIFromResponse(APIResponse);
            if(mainGroupName.equals("主数据管理")){
                try {
                    System.out.println(parseSchemaToJson(openAPI,"PageQuery«Bank»",new HashMap<String,Object>(),false));
                } catch (JsonProcessingException | DocumentException e) {
                    e.printStackTrace();
                }
            }

//            Paths paths = openAPI.getPaths();
//            paths.entrySet().forEach(s -> {
//                if(s.getKey().equals("/user/WfRole/Q004")){
//                    System.out.println(s.getValue());
////                    System.out.println(s.getValue().getPost().getRequestBody());
//                }
//            });

        });
    }
}
