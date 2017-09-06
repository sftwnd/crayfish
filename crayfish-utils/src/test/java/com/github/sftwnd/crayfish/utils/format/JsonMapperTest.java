package com.github.sftwnd.crayfish.utils.format;

import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by ashindarev on 02.02.16.
 */
public class JsonMapperTest {

    @Test
    public void parseObjectTest() throws IOException {
        final String json = "{\"msisdn\":\"abc\", \"objectId\":123}";
        JsonMapperTestObject msg = new JsonMapperTestObject();
        msg.setMsisdn("abc");
        msg.setObjectId(123l);
        JsonMapperTestObject msg1 = JsonMapper.parseObject(json, JsonMapperTestObject.class);
        JsonMapperTestObject msg2 = JsonMapper.parseObject(json.getBytes(), JsonMapperTestObject.class);
        assertEquals("Compare JSON.parse(string).msisdn with POJO", msg.getMsisdn(), msg1.getMsisdn());
        assertEquals("Compare JSON.parse(string).objectId with POJO", msg.getObjectId(), msg1.getObjectId());
        assertEquals("Compare JSON.parse(byte[]).msisdn with POJO", msg.getMsisdn(), msg2.getMsisdn());
        assertEquals("Compare JSON.parse(byte[]).objectId with POJO", msg.getObjectId(), msg2.getObjectId());
    }

    @Test
    public void serializeObjectTest() throws IOException {
        String str = "{\"msisdn\":\"abc\",\"objectId\":123}";
        JsonMapperTestObject msg = new JsonMapperTestObject();
        msg.setMsisdn("abc");
        msg.setObjectId(123l);
        String msg1 = JsonMapper.serializeObject(msg);
        assertEquals("Compare JSON.parse with POJO", str, msg1);
    }
}

class JsonMapperTestObject {

    private String msisdn;
    private Long objectId;


    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

}