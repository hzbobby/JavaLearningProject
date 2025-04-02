package com.bobby.rpc.core.trace.interceptor;

import com.bobby.rpc.core.trace.TraceIdGenerator;
import com.bobby.rpc.core.trace.ZipkinReporter;
import com.bobby.rpc.core.common.trace.TraceContext;

public class ServerTraceInterceptor {
    public static void beforeHandle() {
        String traceId = TraceContext.getTraceId();
        String parentSpanId =TraceContext.getParentSpanId();
        String spanId = TraceIdGenerator.generateSpanId();
        TraceContext.setTraceId(traceId);
        TraceContext.setSpanId(spanId);
        TraceContext.setParentSpanId(parentSpanId);

        // 记录服务端 Span
        long startTimestamp = System.currentTimeMillis();
        TraceContext.setStartTimestamp(String.valueOf(startTimestamp));
    }

    public static void afterHandle(String serviceName) {
        long endTimestamp = System.currentTimeMillis();
        long startTimestamp = Long.valueOf(TraceContext.getStartTimestamp());
        long duration = endTimestamp - startTimestamp;

        // 上报服务端 Span

        ZipkinReporter.reportSpan(
                TraceContext.getTraceId(),
                TraceContext.getSpanId(),
                TraceContext.getParentSpanId(),
                "server-" + serviceName,
                startTimestamp,
                duration,
                serviceName,
                "server"
        );

        // 清理 TraceContext
        TraceContext.clear();
    }
}