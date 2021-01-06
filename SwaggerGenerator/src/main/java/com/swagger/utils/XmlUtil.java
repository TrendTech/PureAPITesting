package com.swagger.utils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlUtil {
    SAXReader reader = new SAXReader();
    Document document;

    private static class csvReadHolder {
        private static final XmlUtil INSTANCE = new XmlUtil();
    }

    private XmlUtil(){
        document = null;
    }

    public void setFile(String filePath) throws DocumentException {
        document = reader.read("config.xml");
    }

    public static final XmlUtil getInstance() {
        return XmlUtil.csvReadHolder.INSTANCE;
    }

    public String readSingleNodeString(String node){
        return document.selectSingleNode(node).getText();
    }

    public List<Node> readNodes(String node){
        return document.selectNodes(node);
    }

    public boolean isEnable(String node){
        return Boolean.parseBoolean(document.selectSingleNode(node).valueOf("@enable"));
    }

    public  Map<String,String> getRegularKey(XmlUtil xml){
        List<Node> nodeList = xml.document.getRootElement().selectNodes("//root/testDataGenerate/specific/key");

        Map<String,String> result = new HashMap<>();
        nodeList.forEach(n ->{
            result.put(n.valueOf("@value"),n.getStringValue());
        });
        return result;
    }

    public  List<String> getIgnoreCases(){
        List<Node> nodeList = XmlUtil.getInstance().document.getRootElement().selectNodes("//root/ignoreCases/casePath");

        List<String> result = new ArrayList<>();
        nodeList.forEach(n ->{
            result.add(n.getStringValue());
        });
        return result;
    }

    public static void main(String[] args) throws DocumentException {
        XmlUtil.getInstance().setFile("config.xml");
        System.out.println(XmlUtil.getInstance().isEnable("//root/testDataGenerate"));
        System.out.println(XmlUtil.getInstance().getRegularKey(XmlUtil.getInstance()));
    }
}
