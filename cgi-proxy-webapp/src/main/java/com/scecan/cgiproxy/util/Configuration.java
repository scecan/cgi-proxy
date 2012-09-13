package com.scecan.cgiproxy.util;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Sandu Cecan
 */
public class Configuration implements Serializable {

    public static final String PROXY_PATH = "proxyPath";
    public static final String REQUEST_URL = "requestUrl";


    private static final long serialVersionUID = 1;

    private static final String XHR_WRAPPER_TEMPLATE;
    static {
        String temp;
        try {
            temp  = IOUtils.toString(Configuration.class.getResourceAsStream("/xmlHttpRequestWrapper.jstemplate"), null);
        } catch (IOException e) {
            temp = "";
        }
        XHR_WRAPPER_TEMPLATE = temp;
    }

    public Configuration() {
        this(null);
        this.proxifyXMLHttpRequest = false;
    }

    public Configuration(String xhrWrapperScript) {
        this._xhrWrapperScript = xhrWrapperScript;
    }


    private boolean proxifyXMLHttpRequest = true;
    private final String _xhrWrapperScript;

    private boolean proxifySWF = false; //todo

    private String bannedImageUrlPatterns; //todo
    /* If an image is banned, then replace it with a 1x1 transparent GIF to show blank space instead of a broken image icon. */
    private boolean returnEmptyGIF = true; //todo


    public boolean isProxifyXMLHttpRequest() {
        return proxifyXMLHttpRequest;
    }
}
