package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class JsonMapperTest {

    @Test
    void testClear() {
        ObjectMapper om = JsonMapper.getObjectMapper();
        JsonMapper.clear();
        assertNotSame(om, JsonMapper.getObjectMapper(), "JsonMapper.getObjectMapper() has to be changed after clear()");
    }

    @Test
    void testParseObject() throws IOException {
        String msisdn = "abc";
        long objectId = 123L;
        final String json = "{\"msisdn\":\""+msisdn+"\", \"objectId\":"+objectId+"}";
        JsonMapperTestObject msg = new JsonMapperTestObject(msisdn, objectId);
        assertEquals(msg, JsonMapper.parseObject(json, JsonMapperTestObject.class), "JsonMapper.parseObject(Class) result has to be equals of POJO object");
        assertEquals(msg, JsonMapper.parseObject(json.getBytes(), JsonMapperTestObject.class), "JsonMapper.parseObject(Class) result has to be equals of POJO object");
    }

    @Test
    void testParseObjectTypeRef() throws IOException {
        JsonMapperTestObjectTypeRef typeRef = new JsonMapperTestObjectTypeRef();
        String msisdn = "abc";
        long objectId = 123L;
        final String json = "{\"msisdn\":\""+msisdn+"\", \"objectId\":"+objectId+"}";
        JsonMapperTestObject msg = new JsonMapperTestObject(msisdn, objectId);
        assertEquals(msg, JsonMapper.parseObject(json, typeRef), "JsonMapper.parseObject(TypeRef) result has to be equals of POJO object");
        assertEquals(msg, JsonMapper.parseObject(json.getBytes(), typeRef), "JsonMapper.parseObject(TypeRef) result has to be equals of POJO object");

    }

    @Test
    void testSerializeObject() throws IOException {
        String msisdn = "def";
        long objectId = 456L;
        final String json = "{\"msisdn\":\""+msisdn+"\", \"objectId\":"+objectId+"}";
        JsonMapperTestObject obj = new JsonMapperTestObject(msisdn, objectId);
        String msg = JsonMapper.serializeObject(obj);
        assertEquals(obj, JsonMapper.parseObject(msg, JsonMapperTestObject.class), "JsonMapper.parseObject(serializedObject(POJO)) result has to be equals of original POJO object");
    }
    
    @Test
    void testGetObjectMapper() throws InterruptedException {
        ObjectMapper mapper = JsonMapper.getObjectMapper();
        assertNotNull(JsonMapper.getObjectMapper(), "JsonMapper.getObjectMapper result has to be not null");
        assertSame(mapper, JsonMapper.getObjectMapper(), "JsonMapper.getObjectMapper result in one thread has to be the same");
        AtomicReference<ObjectMapper> ref = new AtomicReference<>();
        synchronized (ref) {
            new Thread(() -> {
                synchronized (ref) {
                    ref.set(JsonMapper.getObjectMapper());
                    ref.notify();
                }
            }).start();
            ref.wait(100);
        }
        assertNotNull(ref.get(), "JsonMapper.getObjectMapper result in other thread has to be not null");
        assertNotSame(mapper, ref.get(), "JsonMapper.getObjectMapper result in other thread has not to be the same");
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class JsonMapperTestObject {
        private String msisdn;
        private Long objectId;
    }

    static class JsonMapperTestObjectTypeRef extends TypeReference<JsonMapperTestObject> {
        public JsonMapperTestObjectTypeRef() {
            super();
        }
    }

}