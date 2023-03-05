package com.exec.asset.management.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.LRUMap;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility class that converts a Java object to a JSON string.
 */
public final class JsonUtils {
    private JsonUtils() {
    }

    public static ObjectMapper objectMapper() {
        return JsonUtils.Mapper.INSTANCE;
    }

    public static ObjectWriter objectWriter(Class<?> clazz) {
        return JsonUtils.Mapper.writerCache.get(clazz);
    }

    public static String toJson(Object o) {
        return toJson(objectWriter(o.getClass()), o);
    }

    public static String toJson(ObjectWriter ow, Object o) {
        try {
            return ow.writeValueAsString(o);
        }
        catch (Exception var3) {
            throw new IllegalStateException(var3);
        }
    }

    static final class Mapper {
        private static final ObjectMapper INSTANCE;
        static final ClassValue<ObjectReader> readerCache;
        static final LRUMap<JavaType, ObjectReader> readerCacheByJavaType;
        static final ClassValue<ObjectWriter> writerCache;

        private Mapper() {
        }

        static {
            ObjectMapper o = new ObjectMapper();
            o.registerModule(new JavaTimeModule());
            o.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            o.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            o.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            o.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);
            INSTANCE = o;
            readerCache = new ClassValue<>() {
                protected ObjectReader computeValue(Class<?> type) {
                    return JsonUtils.Mapper.INSTANCE.readerFor(type);
                }
            };
            readerCacheByJavaType = new LRUMap(16, 50);
            writerCache = new ClassValue<>() {
                protected ObjectWriter computeValue(Class<?> type) {
                    return JsonUtils.Mapper.INSTANCE.writerFor(type);
                }
            };
        }
    }
}
