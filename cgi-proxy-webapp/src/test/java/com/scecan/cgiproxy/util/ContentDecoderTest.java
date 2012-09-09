package com.scecan.cgiproxy.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;

/**
 * @author Sandu Cecan
 */
public class ContentDecoderTest {

    @Test
    public void testFactoryMethod() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[]{0,1,2,3,4,5,6,7,8,9});

        InputStream decoded1 = ContentDecoder.decode(null, inputStream);
        assertSame(inputStream, decoded1);

//        InputStream decoded2 = ContentDecoder.decode("gzip", inputStream);
//        assertEquals(GZIPInputStream.class, decoded2.getClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryMethod2() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[]{0,1});

        InputStream decoded1 = ContentDecoder.decode("unknownContentEncoding", inputStream);
    }

}
