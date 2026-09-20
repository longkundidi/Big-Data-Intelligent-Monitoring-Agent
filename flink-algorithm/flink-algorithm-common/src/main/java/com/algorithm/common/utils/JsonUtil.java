package com.algorithm.common.utils;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

public class JsonUtil {
    private final static ObjectMapper OBJECT_MAPPER;

    private final static ObjectMapper OBJECT_MAPPER_NON_NULL;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        // 设置时区
        OBJECT_MAPPER.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 设置时间格式
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        OBJECT_MAPPER_NON_NULL = new ObjectMapper();
        // 设置时区
        OBJECT_MAPPER_NON_NULL.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 设置时间格式
        OBJECT_MAPPER_NON_NULL.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        OBJECT_MAPPER_NON_NULL.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER_NON_NULL.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String toJson(Object obj) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    public static String obj2jsonIgnoreNull(Object obj) throws IOException {
        return OBJECT_MAPPER_NON_NULL.writeValueAsString(obj);
    }

    public static <T> T fromJson(String jsonString, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(jsonString, clazz);
    }

    public static Map<String, Object> json2map(String jsonString) throws IOException {
        return OBJECT_MAPPER.readValue(jsonString, new TypeReference<Map<String, Object>>() {
        });
    }

    public static ObjectNode getObjectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }

}
