package com.bobby.myrpc.version8.biz.domain;

import com.alibaba.fastjson2.annotation.JSONType;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String username;
    private Integer age;
}
