package com.bobby.rpc.v1.sample.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Builder
@Data
public class User implements Serializable {
    private Long id;
    private String name;
    private Integer age;
}
