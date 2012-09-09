package com.scecan.cgiproxy.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author Sandu Cecan
 */
public abstract class ContentDecoder {

    private final static class Encoding {
        static final String GZIP = "gzip";
    }

    public static InputStream decode(String httpContentEncoding, InputStream inputStream) throws IOException {
        if (httpContentEncoding == null || httpContentEncoding.trim().isEmpty()) {
            return inputStream;
        } else if (Encoding.GZIP.equals(httpContentEncoding.trim())) {
            return new GZIPInputStream(inputStream);
        } else {
            throw new IllegalArgumentException(String.format("Content-Encoding=%s is not supported.", httpContentEncoding));
        }
    }

}
