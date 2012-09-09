package com.scecan.cgiproxy.parser;

import com.scecan.cgiproxy.util.RequestURL;

import java.io.IOException;
import java.io.InputStream;

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

    public static ResponseParser getParser(String contentType, RequestURL requestURL) {

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
            return new HtmlParser(charset, requestURL);
        } else if (MediaType.CSS.equals(mediaType)) {
            return new CssParser(charset, requestURL);
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

    public abstract InputStream parse(InputStream inputStream) throws IOException;
    
    

}
