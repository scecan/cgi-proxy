package com.scecan.cgiproxy.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;

/**
 * @author Sandu Cecan
 */
public class ContentDecoderTest {

    @Test
    public void testNoEncoding() throws IOException {

        InputStream encodedInputStream = new ByteArrayInputStream(new byte[]{0,1,2,3,4,5,6,7,8,9});
        InputStream decodedInputStream1 = ContentDecoder.decode(null, encodedInputStream);

        assertSame(encodedInputStream, decodedInputStream1);

        InputStream decodedInputStream2 = ContentDecoder.decode("   ", encodedInputStream);

        assertSame(encodedInputStream, decodedInputStream2);
    }

    @Test
    public void testGzipEncoding() throws IOException {
        // unencoded bytes
        byte[] unencodedBytes = new byte[]{0,1,2,3,4,5,6,7,8,9};
        // encode bytes using GZIP
        ByteArrayOutputStream encodedOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(encodedOutputStream);
        IOUtils.pipe(new ByteArrayInputStream(unencodedBytes), gzipOutputStream, new byte[10]);
        gzipOutputStream.finish();
        // encoded bytes
        byte[] encodedBytes = encodedOutputStream.toByteArray();
        // decode encoded bytes using ContentDecoder
        InputStream decodedInputStream = ContentDecoder.decode("gzip", new ByteArrayInputStream(encodedBytes));
        assertSame(GZIPInputStream.class, decodedInputStream.getClass());
        ByteArrayOutputStream decodedOutputStream = new ByteArrayOutputStream();
        IOUtils.pipe(decodedInputStream, decodedOutputStream, new byte[10]);
        // decoded bytes
        byte[] decodedBytes = decodedOutputStream.toByteArray();
        // asserts
        assertEquals(unencodedBytes.length, decodedBytes.length);
        for (int i = 0; i < unencodedBytes.length; i++)
            assertEquals(unencodedBytes[i], decodedBytes[i]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownEncoding() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[]{0,1});

        InputStream unknown = ContentDecoder.decode("unknownContentEncoding", inputStream);
    }

}
