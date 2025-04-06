package com.bobby.rpc.v9.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class RpcRequest implements Serializable {
    // 服务类名，客户端只知道接口名，在服务端中用接口名指向实现类
    private String interfaceName;
    // 方法名
    private String methodName;
    // 参数列表
    private Object[] params;
    // 参数类型
    private Class<?>[] paramsTypes;

    // v6. 包类型
    private RequestType type;

    public RpcRequest(){
        type = RequestType.NORMAL;
    }

    public static RpcRequest heartBeat() {
        return RpcRequest.builder()
                .type(RequestType.HEARTBEAT)
                .build();
    }

    @Getter
    public enum RequestType {
        NORMAL(0),
        HEARTBEAT(1),
        ;
        private final int code;
        RequestType(int code){
            this.code = code;
        }
    }
}