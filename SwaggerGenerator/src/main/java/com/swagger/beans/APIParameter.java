package com.swagger.beans;

import lombok.Data;

@Data
public class APIParameter {
    String inMethod;
    String inValue;
    String inType;
    String inName;
    String schema_Ref;
    String schema_type;
    String schema_array_type;
}
