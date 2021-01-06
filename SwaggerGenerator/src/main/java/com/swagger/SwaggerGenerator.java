package com.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.swagger.beans.APIBeans;
import com.swagger.beans.APIParameter;
import com.swagger.beans.CaseBeans;
import com.swagger.beans.GroupBeans;
import com.swagger.utils.XmlUtil;
import io.restassured.response.Response;
import io.swagger.v3.oas.models.OpenAPI;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;
import com.alibaba.fastjson.JSON;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;

import static com.swagger.SwaggerParser.getSchemafromMap;
import static com.swagger.SwaggerParser.parseParameterIn;
import static io.restassured.RestAssured.*;

public class SwaggerGenerator {

    //去掉特殊字符
    public static String filterName(String fullName){
        String regEx="[\n`~!@#$%^&*()--+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]";
        return fullName==null ? "" : fullName.replaceAll(regEx,"");
    }

    private static CaseBeans getRequestCaseDetail(APIBeans apiBeans, String schema) {
        CaseBeans caseBeans = new CaseBeans();
        Response response = null;
        Map<String,Object> map = new HashMap<>();
        if(apiBeans.getAPI_TYPE().equals("PATH")){
            if(apiBeans.getAPI_METHOD().equals("GET")){
                response = get(apiBeans.getAPI_PATH()+"/"+schema);
            }else if(apiBeans.getAPI_METHOD().equals("DELETE")){
                response = delete(apiBeans.getAPI_PATH()+"/"+schema);
            }
        }else if(apiBeans.getAPI_TYPE().equals("QUERY")){
            if(apiBeans.getAPI_METHOD().equals("GET")){
                response = get(apiBeans.getAPI_PATH()+"?"+schema);
            }
        }else if(apiBeans.getAPI_TYPE().equals("FORMDATA")){
            if(apiBeans.getAPI_METHOD().equals("POST")){

            }
        }else if(apiBeans.getAPI_TYPE().equals("BODY")){
            if(apiBeans.getAPI_METHOD().equals("POST")){
                response = given().header("Content-Type", "application/json").body(schema).post(apiBeans.getAPI_PATH());
//                response.then().log().all();
            }else if(apiBeans.getAPI_METHOD().equals("PUT")){
                response = given().header("Content-Type", "application/json").body(schema).post(apiBeans.getAPI_PATH());
//                response.then().log().all();
            }else if(apiBeans.getAPI_METHOD().equals("GET")){
                response = given().header("Content-Type", "application/json").body(schema).get(apiBeans.getAPI_PATH());
            }else if(apiBeans.getAPI_METHOD().equals("DELETE")){
                response = given().header("Content-Type", "application/json").body(schema).delete(apiBeans.getAPI_PATH());
            }
        }
        if(response!=null){
            try{
                map = (Map)JSON.parse(response.getBody().asString());
                caseBeans.setExpected_code(String.valueOf(response.getStatusCode()));
                if(caseBeans.getExpected_code().equals("200")) {
                    caseBeans.setExpected_R_code(map.get("code").toString());
                    caseBeans.setExpected_msg(map.get("msg").toString().substring(0, 4));
                    caseBeans.setExpected_msg(map.get("msg").toString().substring(0, 4));
                }else{
                    caseBeans.setExpected_R_code(map.get("status").toString());
                    caseBeans.setExpected_msg(map.get("error").toString().substring(0, 4));
                    caseBeans.setExpected_msg(map.get("message").toString().substring(0, 4));
                }
            }catch (Exception e){

            }

        }else{
            caseBeans.setExpected_R_code("200");
            caseBeans.setExpected_msg("空");
            caseBeans.setExpected_msg("空");
        }
        caseBeans.setRequestBody(schema);
        caseBeans.setAPI_PATH(apiBeans.getAPI_PATH().toString());
        if(caseBeans.getExpected_code()==null){
            System.out.println(caseBeans);
        }
        return caseBeans;
   }

    public static void main(String[] args) throws IOException, DocumentException{
        XmlUtil.getInstance().setFile("config.xml");
        final String TestCasePath = XmlUtil.getInstance().readSingleNodeString("//root/global/casePath");;
        final String TestExamplePath = XmlUtil.getInstance().readSingleNodeString("//root/exampleGenerate/examplePath");;
        baseURI = XmlUtil.getInstance().readSingleNodeString("//root/swagger/ServerUrl");
        port = Integer.parseInt(XmlUtil.getInstance().readSingleNodeString("//root/swagger/ServerPort"));
        basePath = XmlUtil.getInstance().readSingleNodeString("//root/swagger/BasePath");

        Response groupResponse = given().header("Content-Type", "application/json").
                get(XmlUtil.getInstance().readSingleNodeString("//root/swagger/Path")).then().extract().response();
        Gson gson = new Gson();
        Type type = new TypeToken<List<GroupBeans>>() {}.getType();
        List<GroupBeans> groupList = gson.fromJson(groupResponse.getBody().print(), type);
        String filePath = XmlUtil.getInstance().readSingleNodeString("//root/global/filePath");
        new File(filePath).createNewFile();
        Writer writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8);
        ICsvListWriter beanWriter = new CsvListWriter(writer,
                CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

        List<String> caseColumn = new ArrayList<>();
        XmlUtil.getInstance().readNodes("//root/caseConfig/column").forEach(s ->
                caseColumn.add(s.getText().toString()));
        List<String> IgnoreCases = XmlUtil.getInstance().getIgnoreCases();

        groupList.forEach(x -> {
                    String mainGroupName = x.getName().substring(0,x.getName().indexOf("("));
                    String mainGroupFilePath = TestCasePath+"\\"+mainGroupName+".csv";
                    String mainGroupExamplePath = TestExamplePath+"\\"+mainGroupName+".csv";
                    ICsvListWriter beanlistWriter = null;
                    ICsvListWriter beanCaseWriter = null;
                    ICsvListWriter beanExampleWrite = null;
                    try {
                        FileUtils.forceMkdir(new File(TestCasePath+"\\"+mainGroupName));
                        FileUtils.forceMkdir(new File(TestExamplePath+"\\"+mainGroupName));
                        File groupFile = new File(mainGroupFilePath);
                        if(!groupFile.exists()) groupFile.createNewFile();
                        beanWriter.write(Arrays.asList(new Object[] { mainGroupName,
                                mainGroupFilePath}));
                        beanWriter.flush();
                        Writer apiWriter = new OutputStreamWriter(new FileOutputStream(mainGroupFilePath), StandardCharsets.UTF_8);
                        beanlistWriter = new CsvListWriter(apiWriter,
                                CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Response APIResponse = given().header("Content-Type", "application/json").
                    get(x.getLocation()).then().extract().response();
                    OpenAPI openAPI = SwaggerParser.getOpenAPIFromResponse(APIResponse);

                    Map<String,Object> map = JSONObject.fromObject(APIResponse.getBody().asString());
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                        APIBeans apiBean = new APIBeans();
                        apiBean.setMAIN_GROUP_NAME(mainGroupName);
                        if(entry.getKey().equals("paths")){
                            Map<String,Object> map1 = (Map<String, Object>) entry.getValue();
                            for (Map.Entry<String, Object> entry1 : map1.entrySet()) {
                                Map<String,Object> map2 = (Map<String, Object>) entry1.getValue();
                                if(entry1.getKey().contains("{")&&entry1.getKey().contains("}")){
//                                    apiBean.setAPI_TYPE("PARAMETER");
                                    apiBean.setAPI_PATH(entry1.getKey().substring(0,entry1.getKey().indexOf("{")-1));
                                }else{
//                                    apiBean.setAPI_TYPE("JSON");
                                    apiBean.setAPI_PATH(entry1.getKey());
                                }
                                apiBean.setAPI_ORIGINAL_PATH(entry1.getKey());

                                for (Map.Entry<String, Object> entry2 : map2.entrySet()) {
                                    Map<String, Object> map3 = (Map<String, Object>) entry2.getValue();
                                    apiBean.setAPI_METHOD(StringUtils.upperCase(entry2.getKey()));
                                    APIParameter apiPara = new APIParameter();
                                    for(Map.Entry<String, Object> entry3 : map3.entrySet()){
                                        if(entry3.getKey().equals("tags")){
                                            apiBean.setSUB_GROUP_NAME(filterName(((JSONArray)entry3.getValue()).getString(0)).replace(mainGroupName,""));
                                        }
                                        if(entry3.getKey().equals("summary")){
                                            apiBean.setAPI_NAME(filterName(entry3.getValue().toString()));
                                        }
                                        if(entry3.getKey().equals("operationId")){
                                            apiBean.setAPI(entry3.getValue().toString());
                                        }
//                                        if(entry3.getKey().equals("consumes")){
//                                            if(entry3.getValue().toString().contains("multipart/form-data")){
////                                                apiBean.setAPI_TYPE("FORMDATA");
//                                            }
//                                        }
                                        if(entry3.getKey().equals("parameters")){
                                            Map<String, Object> map4 = (Map<String, Object>) ((JSONArray)entry3.getValue()).get(0);
                                            apiPara = parseParameterIn(map4);
                                        }
                                    }
//                                    System.out.println(apiPara.toString());
                                    String schemaString = null;
                                    String schemaExample = null;
                                    if(apiPara.getInValue()==null){
                                        apiBean.setAPI_TYPE("BODY");
                                        apiBean.setAPI_SCHEMA("{}");
                                    }else if(apiPara.getInValue().equals("path")){
                                        apiBean.setAPI_TYPE("PATH");
                                        if(apiPara.getInType().equals("integer")){
                                            apiBean.setAPI_SCHEMA("${Integer}");
                                            apiBean.setAPI_SCHEMA_EXAMPLE("{}");
                                        }else if(apiPara.getInType().equals("string")){
                                            apiBean.setAPI_SCHEMA("${RandomString}");
                                            apiBean.setAPI_SCHEMA_EXAMPLE("{}");
                                        }else{
                                            apiBean.setAPI_SCHEMA("${RandomString}");
                                            apiBean.setAPI_SCHEMA_EXAMPLE("{}");
                                        }
                                    }else if(apiPara.getInValue().equals("query")){
                                        apiBean.setAPI_TYPE("QUERY");
                                        if(apiPara.getInType().equals("string")){
                                            apiBean.setAPI_SCHEMA(apiPara.getInName()+"=${Integer}");
                                            apiBean.setAPI_SCHEMA_EXAMPLE("{}");
                                        }else if(apiPara.getInType().equals("integer")){
                                            apiBean.setAPI_SCHEMA(apiPara.getInName()+"=${Integer}");
                                            apiBean.setAPI_SCHEMA_EXAMPLE("{}");
                                        }
                                    }else if(apiPara.getInValue().equals("formData")){
                                        apiBean.setAPI_TYPE("FORMDATA");
                                        if(apiPara.getInType().equals("file")){
                                            apiBean.setAPI_SCHEMA("${FilePath}");
                                            apiBean.setAPI_SCHEMA_EXAMPLE("{}");
                                        }
                                    }else if(apiPara.getInValue().equals("body")){
                                        apiBean.setAPI_TYPE("BODY");
//                                        System.out.println(apiBean.getAPI_PATH()+" --- "+apiPara.getSchema_Ref());
                                        if(apiPara.getSchema_type()==null){
                                            try {
                                                schemaString = getSchemafromMap(SwaggerParser.parseSchemaToJson(openAPI,apiPara.getSchema_Ref(),new HashMap<String,Object>(),false));
                                                schemaExample = getSchemafromMap(SwaggerParser.parseSchemaToJson(openAPI,apiPara.getSchema_Ref(),new HashMap<String,Object>(),true));
                                                apiBean.setAPI_SCHEMA(schemaString);
                                                apiBean.setAPI_SCHEMA_EXAMPLE(schemaExample);
                                            } catch (JsonProcessingException | DocumentException e) {
                                                e.printStackTrace();
                                            }
                                        }else if(apiPara.getSchema_type().equals("array")){
                                            if(apiPara.getSchema_array_type()==null){
                                                try {
                                                    schemaString = "[" + getSchemafromMap(SwaggerParser.parseSchemaToJson(openAPI, apiPara.getSchema_Ref(), new HashMap<String, Object>(), false)) + "]";
                                                    schemaExample = "["+ getSchemafromMap(SwaggerParser.parseSchemaToJson(openAPI,apiPara.getSchema_Ref(),new HashMap<String,Object>(),true)) +"]";
                                                } catch (JsonProcessingException e) {
                                                    e.printStackTrace();
                                                } catch (DocumentException e) {
                                                    e.printStackTrace();
                                                }
                                                apiBean.setAPI_SCHEMA(schemaString);
                                                apiBean.setAPI_SCHEMA_EXAMPLE(schemaExample);
                                            } else if(apiPara.getSchema_array_type().equals("string")){
                                                schemaString = "${array}";
                                                apiBean.setAPI_SCHEMA(schemaString);
                                                apiBean.setAPI_SCHEMA_EXAMPLE("{}");
                                            }
                                        }
                                    }
                                }
//
                                String subGroupFilePath = TestCasePath+"\\"+apiBean.getMAIN_GROUP_NAME()+"\\"
                                        +apiBean.getSUB_GROUP_NAME();
                                String subGroupExampleFilePath = TestExamplePath+"\\"+apiBean.getMAIN_GROUP_NAME()+"\\"
                                        +apiBean.getSUB_GROUP_NAME();
                                try {
                                    IgnoreCases.forEach(ig ->{
                                        if((apiBean.getAPI_PATH()!=null)&&(apiBean.getAPI_ORIGINAL_PATH().contains(ig))){
                                            apiBean.setAPI_PATH(null);
                                        }
                                    });
                                    FileUtils.forceMkdir(new File(subGroupFilePath));
                                    FileUtils.forceMkdir(new File(subGroupExampleFilePath));
                                    if(apiBean.getAPI_PATH()!=null){
                                    String apiPath = subGroupFilePath+"\\"+
                                            apiBean.getAPI_NAME()+"_"+
                                            apiBean.getAPI()+".csv";
                                    String apiExamplePath = subGroupExampleFilePath+"\\"+
                                            apiBean.getAPI_NAME()+"_"+
                                            apiBean.getAPI()+".csv";
                                    File apiFile = new File(apiPath);
                                    File exampleFile = new File(apiExamplePath);
                                    if(!apiFile.exists()){
                                        apiFile.createNewFile();
                                    }
                                    if(!exampleFile.exists()){
                                        exampleFile.createNewFile();
                                    }
                                        beanlistWriter.write(Arrays.asList(new Object[] {
                                                apiBean.getSUB_GROUP_NAME(),
                                                apiBean.getAPI_NAME(),
                                                apiBean.getAPI(),
                                                apiBean.getAPI_PATH(),
                                                apiBean.getAPI_METHOD(),
                                                apiBean.getAPI_TYPE()}));
                                        beanlistWriter.flush();

                                    CsvPreference MY_PREFERENCE = (new CsvPreference.Builder('\'', 59, "\n")).build();
                                    CsvPreference excelPreference = new CsvPreference.Builder(MY_PREFERENCE).build();
                                    beanCaseWriter = new CsvListWriter(new FileWriter(apiFile),
                                            excelPreference);
                                    beanExampleWrite = new CsvListWriter(new FileWriter(exampleFile),
                                            excelPreference);
                                  if(apiBean.getAPI_SCHEMA_EXAMPLE()!=null){
//                                            apiBean.setAPI_SCHEMA_EXAMPLE(apiBean.getAPI_SCHEMA_EXAMPLE().replace("\"","\'"));
                                        }
                                    beanExampleWrite.write(Arrays.asList(RandomGenerator.addRandomToJson(apiBean.getAPI_SCHEMA_EXAMPLE(),true)));
                                    beanExampleWrite.flush();
                                    if(apiFile.length() == 0) {
                                        beanCaseWriter.write(caseColumn);
                                        beanCaseWriter.flush();
                                        if (apiBean.getAPI_SCHEMA() != null) {
//                                            apiBean.setAPI_SCHEMA(apiBean.getAPI_SCHEMA().replace("\"","\'"));
                                        }
                                        if (XmlUtil.getInstance().isEnable("//root/testDataGenerate")) {
                                            int i = 0;
                                            int loops = Integer.parseInt(XmlUtil.getInstance().readSingleNodeString("//root/testDataGenerate/loops"));
                                            while (i < loops) {
                                                String schema = RandomGenerator.addRandomToJson(apiBean.getAPI_SCHEMA(), false);
                                                CaseBeans caseBeans = new CaseBeans();
                                                caseBeans.setAPI_PATH(apiBean.getAPI_PATH());
//                                                System.out.println(apiBean);
//                                                caseBeans = getRequestCaseDetail(apiBean,
//                                                        schema);
                                                beanCaseWriter.write(Arrays.asList(new Object[]{
                                                        "自动生成用例",
                                                        RandomGenerator.addRandomToJson(apiBean.getAPI_SCHEMA(), false),
                                                        "200", "200", "空", "空"
//                                                        schema,
//                                                        caseBeans.getExpected_code(),
//                                                        caseBeans.getExpected_R_code(),
//                                                        caseBeans.getExpected_msg(),
//                                                        caseBeans.getExpected_body()
                                                }));
                                                i++;
                                            }
                                            beanCaseWriter.flush();
                                        }
                                    }
                                    }
                                } catch (IOException | ParseException | DocumentException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
    }
}
