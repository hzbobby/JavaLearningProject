package com.bobby.rpc.core.trace.interceptor;

import com.bobby.rpc.core.trace.TraceIdGenerator;
import com.bobby.rpc.core.trace.ZipkinReporter;
import com.bobby.rpc.core.common.trace.TraceContext;

public class ClientTraceInterceptor {
    public static void beforeInvoke() {
        String traceId = TraceContext.getTraceId();
        if (traceId == null) {
            traceId = TraceIdGenerator.generateTraceId();
            TraceContext.setTraceId(traceId);
        }
        String spanId = TraceIdGenerator.generateSpanId();
        TraceContext.setSpanId(spanId);

        // 记录客户端 Span
        long startTimestamp = System.currentTimeMillis();
        TraceContext.setStartTimestamp(String.valueOf(startTimestamp));
    }

    public static void afterInvoke(String serviceName) {
        long endTimestamp = System.currentTimeMillis();
        long startTimestamp = Long.valueOf(TraceContext.getStartTimestamp());
        long duration = endTimestamp - startTimestamp;

        // 上报客户端 Span
        ZipkinReporter.reportSpan(
                TraceContext.getTraceId(),
                TraceContext.getSpanId(),
                TraceContext.getParentSpanId(),
                "client-" + serviceName,
                startTimestamp,
                duration,
                serviceName,
                "client"
        );

        // 清理 TraceContext
        TraceContext.clear();
    }
}