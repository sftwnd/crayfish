/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json.deserialize;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.sftwnd.crayfish.common.format.formatter.TemporalFormatter;
import com.github.sftwnd.crayfish.common.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by ashindarev on 29.02.16.
 */
class JsonZonedDateTimeDeserializerTest {

    @Test
    void testJsonZonedDateTimeDeserializer() throws IOException {
        ZonedDateTime zdt = ZonedDateTime.now();
        String dateStr = new TemporalFormatter(DateTimeFormatter.ISO_ZONED_DATE_TIME).format(zdt, ZoneId.systemDefault());
        JsonZonedDateTimeDeserializerTestDataObject object = new JsonMapper().parseObject("{\"zdt\":\""+dateStr+"\"}", JsonZonedDateTimeDeserializerTestDataObject.class);
        assertEquals(
                zdt, Optional.ofNullable(object).map(JsonZonedDateTimeDeserializerTestDataObject::getZdt).orElse(null),
                "JsonZonedDateTimeDeserializer has to parse ZonedDateTime with right value"
        );
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class JsonZonedDateTimeDeserializerTestDataObject {
        @JsonDeserialize(using = JsonZonedDateTimeDeserializer.class)
        ZonedDateTime zdt;
    }

}