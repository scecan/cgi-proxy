package com.scecan.cgiproxy.util;

import java.io.*;

/**
 * @author Sandu Cecan
 */
public class IOUtils {

    public static void pipe(InputStream is, OutputStream os, byte[] buffer) throws IOException {
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.flush();
    }

    public static void pipe(Reader reader, Writer writer, char[] buffer) throws IOException {
        int charsRead;
        while ((charsRead = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, charsRead);
        }
        writer.flush();
    }
    
    public static InputStream toInputStream(String input, String charset) throws UnsupportedEncodingException {
        return new ByteArrayInputStream((charset == null) ? input.getBytes() : input.getBytes(charset));
    }

    public static String toString(InputStream inputStream, String charset) throws IOException {
        StringWriter writer = new StringWriter();
        Reader reader = (charset != null) ? new InputStreamReader(inputStream, charset) : new InputStreamReader(inputStream);
        IOUtils.pipe(reader, writer, new char[1024]);
        return writer.toString();
    }

}
