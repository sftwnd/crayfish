/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by ashindarev on 29.02.16.
 */
public class JsonDateDeserializerTest {
/*
    private static final Logger logger = LoggerFactory.getLogger(JsonDateDeserializerTest.class);

    private static final String getTimeZone() {
        return Calendar.getInstance().getTimeZone().getDisplayName();
    }

    private static final String timeZone = getTimeZone();

    private static final Random random = new Random();


    @BeforeEach
    public void setUp() {
        JsonDateDeserializer.clearTimeZoneId();
        JsonDateDeserializer.clearDefaultTimeZoneId();
    }

    @AfterEach
    public void tearDown() {
        JsonDateDeserializer.clearTimeZoneId();
        JsonDateDeserializer.clearDefaultTimeZoneId();
    }

    @Test
    public void testDeserialize() throws Exception {
        String[] dates = new String[]{
                "2016-02-29"
                , "2016-02-29T19"
                , "2016-02-29T19:37"
                , "2016-02-29T19:37:39"
                , "2016-02-29T19:37:39.418"
                , "2016-02-29+04:00"
                , "2016-02-29T19+05:00"
                , "2016-02-29T19:37+06:00"
                , "2016-02-29T19:37:39+07:00"
                , "2016-02-29T19:37:39.418+08:00"
                , "2016-02-29T19:37:39.418Z"
        };
        String[] dateFormats = new String[]{
                "yyyy-MM-dd"
                , "yyyy-MM-dd'T'HH"
                , "yyyy-MM-dd'T'HH:mm"
                , "yyyy-MM-dd'T'HH:mm:ss"
                , "yyyy-MM-dd'T'HH:mm:ss.SSS"
                , "yyyy-MM-ddXXX"
                , "yyyy-MM-dd'T'HHXXX"
                , "yyyy-MM-dd'T'HH:mmXXX"
                , "yyyy-MM-dd'T'HH:mm:ssXXX"
                , "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
                , "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
        };
        for (int i = 0; i < dates.length; i++) {
            try {
                for (String timeZone : new String[]{"UTC", "GMT+05:00", "Europe/Moscow"}) {
                    DateFormat dateFormat = new SimpleDateFormat(dateFormats[i]);
                    dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
                    Date date = dateFormat.parse(dates[i]);
                    String json = new StringBuilder("{\"date\":\"").append(dates[i]).append("\"}").toString();
                    JsonDateDeserializer.setTimeZoneId(timeZone);
                    JsonDateDeserializerTestDateObject obj = new ObjectMapper().readerFor(JsonDateDeserializerTestDateObject.class).readValue(json);
                    assertEquals(date, obj.getDate(), "Check POJO('" + json + "', tz:" + timeZone + ").getDate equals '" + dates[i] + "'");
                }
            } finally {
                JsonDateDeserializer.clearTimeZoneId();
            }
        }
    }

    @Test
    public void testDeserializeDifferentThreads() throws Exception {
        final String json = "{\"date\":\"2016-02-29T19:37:39.418\"}";
        final Date[] results = new Date[5];
        final String timeZone = "UTC+03:00";
        final String unuzedTimeZone = "UTC-03:00";
        final CountDownLatch threadStartedLatch = new CountDownLatch(4);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch finishLatch = new CountDownLatch(results.length);
        class Task implements Runnable {
            private int id;

            public Task(int id) {
                this.id = id;
            }

            @Override
            public void run() {
                try {
                    threadStartedLatch.countDown();
                    startLatch.await(10, TimeUnit.SECONDS);
                    if (id == 3) {
                        JsonDateDeserializer.setTimeZoneId("GMT+03:00");
                    }
                    if (id == 4) {
                        JsonDateDeserializer.setTimeZoneId("GMT+04:00");
                    }
                    String taskTimeZone = JsonDateDeserializer.getTimeZoneId();
                    JsonDateDeserializerTestDateObject obj = new ObjectMapper().readerFor(JsonDateDeserializerTestDateObject.class).readValue(json);
                    results[id] = obj.getDate();
                    logger.trace("Task[{}]: timeZone={}, date={}", id, taskTimeZone, DateSerializeUtility.getDateSerializeUtility(taskTimeZone, null).serialize(obj.getDate()));
                    finishLatch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        JsonDateDeserializer.setTimeZoneId(unuzedTimeZone);
        List<Thread> threadList = new ArrayList<>();
        // Task[0,1] - timeZone (GMT+03:00), последоваельно
        // Task[2] - UTC (Default)
        // Task[3] - GMT+03:00
        // Task[4] - GMT+04:00
        threadList.add(
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                JsonDateDeserializer.setTimeZoneId(timeZone);
                                new Task(0).run();
                                // Выполняем в текущей нити.
                                new Thread(new Task(1)).run();
                            }
                        }
                )
        );
        for (int i = 2; i < results.length; i++) {
            threadList.add(new Thread(new Task(i)));
        }
        for (Thread thread : threadList) {
            thread.setDaemon(true);
            thread.start();
        }
        threadStartedLatch.await(10, TimeUnit.SECONDS);
        startLatch.countDown();
        finishLatch.await();
        assertEquals(results[0], results[1], "Default date has to be equal date translated in the same thread.");
        assertEquals(results[0], results[3], "Default date has to be equal date translated in the same timeZone.");
        assertNotEquals(results[0], results[2], "Default date has not to be equal date translated in the other thread without timeZone change.");
        assertEquals(3600 * 1000L, results[3].getTime() - results[4].getTime(), "Date with timeZones GMT+03:00 and GMT+04:00 has to be diffeent for a hour.");
        for (Date date : results) {
            logger.trace(DateSerializeUtility.defaultSerialize(date));
        }
    }

    @Test
    public void testDeserializeException() {
        assertThrows(JsonMappingException.class,
                () -> new ObjectMapper().readerFor(JsonDateDeserializerTestDateObject.class).readValue("{\"date\":\"oops\"}")
        );
    }

    @Test
    public void testSetGetTimeZoneId() throws Exception {
        String[] emptyTimeZone = new String[] {null, "", "  "};
        try {
            for (int i = 0; i < 17; i++) {
                // Check setTimeZone(random Value)
                String timeZone = TimeZone.getAvailableIDs()[random.nextInt(TimeZone.getAvailableIDs().length)];
                JsonDateDeserializer.setTimeZoneId(timeZone);
                assertEquals(timeZone, JsonDateDeserializer.getTimeZoneId(), "Check [get/set]TimeZoneId(" + timeZone + ") value");
                // Check setTimeZone(empty Value)
                timeZone = TimeZone.getAvailableIDs()[random.nextInt(TimeZone.getAvailableIDs().length)];
                JsonDateDeserializer.setDefaultTimeZoneId(timeZone);
                JsonDateDeserializer.setTimeZoneId(emptyTimeZone[i % emptyTimeZone.length]);
                assertEquals(timeZone, JsonDateDeserializer.getTimeZoneId(), "Check [get/set]TimeZoneId(empty timeZone) value");
            }
        } finally {
            JsonDateDeserializer.setDefaultTimeZoneId("UTC");
        }
    }

    @Test
    public void testSetLocalTimeZone() throws Exception {
        String timeZone = Calendar.getInstance().getTimeZone().getID();
        JsonDateDeserializer.setLocalTimeZone();
        assertEquals(timeZone, JsonDateDeserializer.getTimeZoneId(), "Check setLocalTimeZoneId = \""+timeZone+"\"");
    }

    @Test
    public void testClearTimeZoneId() throws Exception {
        for (int i=0; i<10; i++) {
            String timeZone = TimeZone.getAvailableIDs()[random.nextInt(TimeZone.getAvailableIDs().length)];
            JsonDateDeserializer.setTimeZoneId(timeZone);
            JsonDateDeserializer.clearTimeZoneId();
            assertEquals(JsonDateDeserializer.getDefaultTimeZoneId(), JsonDateDeserializer.getTimeZoneId(), "Check clearDefaultTimeZoneId() value = defaultTimeZone");
        }
    }

    @Test
    public void testSetGetDefaultTimeZoneIdToLocal() throws Exception {
        for (int i=0; i<10; i++) {
            String timeZone = TimeZone.getAvailableIDs()[random.nextInt(TimeZone.getAvailableIDs().length)];
            JsonDateDeserializer.setDefaultTimeZoneId(timeZone);
            assertEquals(timeZone, JsonDateDeserializer.getDefaultTimeZoneId(), "Check [get/set]DefaultTimeZoneId("+timeZone+") value");
        }
    }

    @Test
    public void testClearDefaultTimeZoneId() throws Exception {
        for (int i=0; i<10; i++) {
            String timeZone = TimeZone.getAvailableIDs()[random.nextInt(TimeZone.getAvailableIDs().length)];
            JsonDateDeserializer.setDefaultTimeZoneId(timeZone);
            JsonDateDeserializer.clearDefaultTimeZoneId();
            assertEquals("UTC", JsonDateDeserializer.getDefaultTimeZoneId(), "Check clearDefaultTimeZoneId() value is UTC");
        }
    }

    @Test
    public void testSetGetTimeZoneIdUtcOffset() throws Exception {
        JsonDateDeserializer.setTimeZoneId("UTC+03:00");
        assertEquals("GMT+03:00", JsonDateDeserializer.getTimeZoneId(), "Check [set/get]TimeZoneId(UTC+OFFSET)");
    }

    @Test
    public void testSetGetDefaultTimeZoneIdUtcOffset() throws Exception {
        JsonDateDeserializer.setDefaultTimeZoneId("UTC+03:00");
        assertEquals("GMT+03:00", JsonDateDeserializer.getDefaultTimeZoneId(), "Check [set/get]DefaultTimeZoneId(UTC+OFFSET)");
    }

    @Test
    public void testSetDefaultTimeZoneIdToLocal() {
        String localTimeZone = Calendar.getInstance().getTimeZone().getID();
        for (int i=0; i<10; i++) {
            String timeZone = TimeZone.getAvailableIDs()[random.nextInt(TimeZone.getAvailableIDs().length)];
            JsonDateDeserializer.setDefaultTimeZoneId(timeZone);
            JsonDateDeserializer.setDefaultTimeZoneIdToLocal();
            assertEquals(localTimeZone, JsonDateDeserializer.getDefaultTimeZoneId(), "Check clearDefaultTimeZoneId() value is \""+localTimeZone+"\".");
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class JsonDateDeserializerTestDateObject {

        @JsonSerialize(using=JsonDateSerializer.class)
        @JsonDeserialize(using=JsonDateDeserializer.class)
        private Date date;

    }
*/
}