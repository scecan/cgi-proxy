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
        static final String HTML    =   "text/html";
        static final String CSS     =   "text/css";
    }

    public static ResponseParser createParser(String contentType) {

        String charset = null;
        String mediaType = null;
        if (contentType != null) {
            int index = contentType.indexOf(CHARSET_IDENTIFIER);
            if (index != -1) {
                charset = contentType.substring(index+CHARSET_IDENTIFIER.length());
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

    public String getCharset() {
        return charset;
    }

    public abstract InputStream parse(InputStream inputStream, String proxyPath, URL hostURL) throws IOException;
    
    

}
