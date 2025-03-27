package com.bobby.biza.aspect;

import lombok.Data;

import java.util.Objects;

@Data
public class InvokeCtx {
    private String className;      // 类名
    private String methodName;     // 方法名
    private String paramTypesJson; // 参数类型JSON字符串
    private String argsJson;       // 参数值JSON字符串

    // 创建Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String className;
        private String methodName;
        private String paramTypesJson;
        private String argsJson;

        public Builder className(String className) {
            this.className = className;
            return this;
        }

        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public Builder paramTypes(String paramTypes) {
            this.paramTypesJson = paramTypes;
            return this;
        }

        public Builder args(String args) {
            this.argsJson = args;
            return this;
        }


        public InvokeCtx build() {
            Objects.requireNonNull(className, "className cannot be null");
            Objects.requireNonNull(methodName, "methodName cannot be null");

            if (paramTypesJson == null) {
                paramTypesJson = "[]"; // 默认空数组
            }

            if (argsJson == null) {
                argsJson = "[]"; // 默认空数组
            }
            InvokeCtx invokeCtx = new InvokeCtx();
            invokeCtx.className = className;
            invokeCtx.methodName = methodName;
            invokeCtx.paramTypesJson = paramTypesJson;
            invokeCtx.argsJson = argsJson;

            return invokeCtx;
        }
    }
}
