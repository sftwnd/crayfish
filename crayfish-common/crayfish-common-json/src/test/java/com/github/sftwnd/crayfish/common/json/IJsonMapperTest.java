/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class IJsonMapperTest {

    @Test
    void testParseObjectWithStringClass() throws IOException {
        IJsonMapper mapper = spy(IJsonMapper.class);
        String str = "teststrClass";
        when(mapper.parseObject(any(byte[].class), (Class<String>) any())).thenReturn("#"+str);
        assertEquals("#"+str, mapper.parseObject(str, String.class), "Result has to be conctructed right");
        verify(mapper, times(1)).parseObject(any(byte[].class), (Class<String>) any());
        verify(mapper, times(1)).parseObject(anyString(), (Class<String>) any());
        assertEquals(2, mockingDetails(mapper).getInvocations().size(), "IJsonMapper has to be invoked twice");
        assertNull(mapper.parseObject((String)null, String.class), "Result for IJsonMapper.parse(null,Class) has to be null");
    }

    @Test
    void testParseObjectWithStringTypeReference() throws IOException {
        IJsonMapper mapper = spy(IJsonMapper.class);
        String str = "teststrType";
        when(mapper.parseObject(any(byte[].class), (TypeReference<String>) any())).thenReturn("#"+str);
        assertEquals("#"+str, mapper.parseObject(str, (TypeReference<String>)null), "Result has to be conctructed right");
        verify(mapper, times(1)).parseObject(any(byte[].class), (TypeReference<String>) any());
        verify(mapper, times(1)).parseObject(anyString(), (TypeReference<String>) any());
        assertEquals(2, mockingDetails(mapper).getInvocations().size(), "IJsonMapper has to be invoked twice");
        assertNull(mapper.parseObject((String)null, mock(TypeReference.class)), "Result for IJsonMapper.parse(null,TypeReference) has to be null");
    }

}