/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class IJsonZonedMapperTest {

    @Test
    void testParseObjectWithZoneIdStringClass() throws IOException {
        IJsonZonedMapper mapper = spy(IJsonZonedMapper.class);
        String str = "testZnStrClass";
        when(mapper.parseObject(any(ZoneId.class), any(byte[].class), (Class<String>) any())).thenReturn("#"+str);
        assertEquals("#"+str, mapper.parseObject(ZoneId.systemDefault(), str, String.class), "Result has to be conctructed right");
        verify(mapper, times(1)).parseObject(any(ZoneId.class), any(byte[].class), (Class<String>) any());
        verify(mapper, times(1)).parseObject(any(ZoneId.class), anyString(), (Class<String>) any());
        assertEquals(2, Mockito.mockingDetails(mapper).getInvocations().size(), "IJsonMapper has to be invoked twice");
        assertNull(mapper.parseObject(ZoneId.systemDefault(), (String)null, String.class), "Result for IJsonZonedMapper.parse(Zoneid, null, Class) has to be null");
    }

    @Test
    void testParseObjectWithZoneIdStringTypeReference() throws IOException {
        IJsonZonedMapper mapper = spy(IJsonZonedMapper.class);
        String str = "testZnTrStrClass";
        when(mapper.parseObject(any(ZoneId.class), any(byte[].class), (TypeReference<String>) any())).thenReturn("#"+str);
        assertEquals("#"+str, mapper.parseObject(ZoneId.systemDefault(), str, (TypeReference<String>)null), "Result has to be conctructed right");
        verify(mapper, times(1)).parseObject(any(ZoneId.class), any(byte[].class), (TypeReference<String>) any());
        verify(mapper, times(1)).parseObject(any(ZoneId.class), anyString(), (TypeReference<String>) any());
        assertEquals(2, Mockito.mockingDetails(mapper).getInvocations().size(), "IJsonMapper has to be invoked twice");
        assertNull(mapper.parseObject(ZoneId.systemDefault(), (String)null, mock(TypeReference.class)), "Result for IJsonZonedMapper.parse(Zoneid, null, TypeReference) has to be null");
    }

    @Test
    void testSerializeObjectZoneId() throws IOException {
        IJsonZonedMapper mapper = spy(IJsonZonedMapper.class);
        String str = "testZnTrSerialize";
        when(mapper.formatObject(any(), any())).thenReturn("@"+str);
        assertEquals("@"+str, mapper.formatObject(str), "Result has to be conctructed right");
        verify(mapper, times(1)).formatObject(any());
        verify(mapper, times(1)).formatObject(any(), any());
        assertEquals(2, Mockito.mockingDetails(mapper).getInvocations().size(), "IJsonZonedMapper has to be invoked twice");
        assertNull(mapper.formatObject(null), "Result for IJsonZonedMapper.serializeObject(Zoneid, null) has to be null");
    }

}