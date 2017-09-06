package com.github.sftwnd.crayfish.messaging.impl;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ashindarev on 02.02.17.
 */
public class DefaultMessageTest {

    private static final Integer tag = new Integer(100);
    private static final Object  payload = "Value: 100";
    private static final DefaultMessage<Integer, Object> defaultMessage = new DefaultMessage<>(new Integer(tag), new String(payload.toString()));

    @Test
    public void getTagTest() throws Exception {
        Assert.assertEquals("Tag has to be equal", tag, defaultMessage.getTag());
    }

    @Test
    public void getPayloadTest() throws Exception {
        Assert.assertEquals("Payload has to be equal", payload, defaultMessage.getPayload());
    }

    @Test
    public void equalsTest() throws Exception {
        Assert.assertEquals("New message(message) has to be equals original one", defaultMessage, new DefaultMessage<Integer, Object>(defaultMessage));
        Assert.assertEquals("New message(tag, payload) has to be equals original one", defaultMessage, new DefaultMessage<Integer, Object>(defaultMessage.getTag(), defaultMessage.getPayload()));
    }

    @Test
    public void hashCodeTest() throws Exception {
        Assert.assertEquals("New message hashcode has to be equals original message hashcode", defaultMessage.hashCode(), new DefaultMessage<Integer, Object>(defaultMessage.getTag(), defaultMessage.getPayload()).hashCode());
    }

}