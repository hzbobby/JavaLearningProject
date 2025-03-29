package com.bobby.myrpc.version6.domain;

import com.alibaba.fastjson2.annotation.JSONType;
import com.bobby.myrpc.version6.utils.UserReader;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@JSONType(deserializer = UserReader.class)
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String username;
    private Integer age;
}
