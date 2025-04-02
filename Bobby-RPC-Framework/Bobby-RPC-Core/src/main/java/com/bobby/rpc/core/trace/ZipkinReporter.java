package com.bobby.rpc.core.trace;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Slf4j
public class ZipkinReporter {
    private static final String ZIPKIN_URL = "http://192.168.160.128:9411/api/v2/spans"; // Zipkin 服务器地址
    private static final AsyncReporter<Span> reporter;

    static {
        // 初始化 Zipkin 上报器
        OkHttpSender sender = OkHttpSender.create(ZIPKIN_URL);
        reporter = AsyncReporter.create(sender);
    }

    private static void InternalOkHttpSender() {
    }

    /**
     * 上报 Span 数据到 Zipkin
     */
    public static void reportSpan(String traceId, String spanId, String parentSpanId,
                                  String name, long startTimestamp, long duration,
                                  String serviceName,String type) {
        Span span = Span.newBuilder()
                .traceId(traceId)
                .id(spanId)
                .parentId(parentSpanId)
                .name(name)
                .timestamp(startTimestamp * 1000) // Zipkin 使用微秒
                .duration(duration * 1000) // Zipkin 使用微秒
                .putTag("service",serviceName)
                .putTag("type",type)
                .build();
        reporter.report(span);
        log.info("当前traceId:{}正在上报日志-----",traceId);
    }

    public static void close() {
        reporter.close();
    }
}