package com.swagger.beans;

import lombok.Data;

@Data
public class CaseBeans {
    String mainGroupName;
    String subGroupName;
    String API_PATH;
    String API_NAME;
    String API;
    String caseName;
    String requestBody;
    String expected_code;
    String expected_R_code;
    String expected_msg;
    String expected_body;
}
