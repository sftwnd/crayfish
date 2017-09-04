package com.sftwnd.crayfish.utils.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by ashindarev on 09.02.16.
 */
public class JsonDateSerializerTest {

    @Test
    public void serializeTest() throws IOException {
        final Date currentDate = new Date();
        ObjectMapper mapper = new ObjectMapper();
        JsonDateSerializerTestDateObject obj1 = new JsonDateSerializerTestDateObject(currentDate);
        String str1= mapper.writeValueAsString(obj1);
        JsonDateSerializerTestDateObject obj2 = mapper.readerFor(JsonDateSerializerTestDateObject.class).readValue(str1);
        assertEquals("Check POJO(Date) after reserialization with base one", obj1.getDate(), obj2.getDate());
    }

}

class JsonDateSerializerTestDateObject {
    private Date date;

    public JsonDateSerializerTestDateObject() {
        this(null);
    }
    public JsonDateSerializerTestDateObject(Date date) {
        setDate(date);
    }
    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getDate() {
        return date;
    }
    @JsonDeserialize(using=JsonDateDeserializer.class)
    public void setDate(Date date) {
        this.date = date;
    }
}