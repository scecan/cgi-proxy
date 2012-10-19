package com.scecan.cgiproxy.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Sandu Cecan
 */
public abstract class ResponseParser {

    protected final String charset;

    private static final String CHARSET_IDENTIFIER = "charset=";

    private static final class MediaType {
        static final String HTML = "text/html";
        static final String CSS = "text/css";
    }

    /**
     * Creates a proper {@link ResponseParser} implementation depending on "Content-Type" HTTP header value.
     *
     * @param contentType value of HTTP "Content-Type" header field
     * @return proper implementation of {@link ResponseParser} or {@code null} if an implementation this "Content-Type" is not found
     */
    public static ResponseParser createParser(String contentType) {

        String charset = null;
        String mediaType = null;
        if (contentType != null) {
            int index = contentType.indexOf(CHARSET_IDENTIFIER);
            if (index != -1) {
                charset = contentType.substring(index + CHARSET_IDENTIFIER.length());
            }
            index = contentType.indexOf(";");
            if (index == -1) {
                mediaType = contentType;
            } else {
                mediaType = contentType.substring(0, index);
            }
        }

        if (MediaType.HTML.equals(mediaType)) {
            return new HtmlParser(charset);
        } else if (MediaType.CSS.equals(mediaType)) {
            return new CssParser(charset);
        } else {
            return null;
        }
    }

    protected ResponseParser(String charset) {
        this.charset = charset;
    }

    /**
     * @return the charset value from "Content-Type" header field or null if it is not defined
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Parses the input stream and proxifies all URL in it
     * @param inputStream the input stream to parse
     * @param proxyPath the path to the servlet which is used to proxify URLs
     * @param hostURL the host URL of this input stream
     * @return a new parsed input stream
     * @throws IOException
     */
    public abstract InputStream parse(InputStream inputStream, String proxyPath, URL hostURL) throws IOException;


}
