package com.swagger.beans;

import lombok.Data;

@Data
public class APIBeans {
    String API; //接口
    String API_NAME; //接口名
    String API_TYPE; //接口类型
    String API_METHOD; //接口方法
    String API_PATH; //接口路径
    String API_ORIGINAL_PATH; //接口原始路径
    String MAIN_GROUP_NAME; //主类名
    String SUB_GROUP_NAME; //子类名
    String API_SCHEMA; //接口schema
    String API_SCHEMA_EXAMPLE; //接口生成例子schema
}
