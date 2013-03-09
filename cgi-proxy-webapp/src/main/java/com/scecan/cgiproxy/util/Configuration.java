package com.scecan.cgiproxy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

/**
 * @author Sandu Cecan
 */
public class Configuration implements Serializable {

    private static final long serialVersionUID = 1;

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private static final String CONFIG_FILE = "/cgi-proxy-config.properties";

    private static interface ConfigNames {
        static final String EXCLUDED_HTTP_HEADERS = "http.headers.excluded";
    }

    private static final Properties config = new Properties();
    static {
        InputStream is = null;
        try {
            is = Configuration.class.getResourceAsStream(CONFIG_FILE);
            config.load(is);
        } catch (IOException e) {
            logger.error("Could not load configuration file", e);
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
        }
    }

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

    private final Set<String> excludedHttpHeaders = new HashSet<String>();


    public Configuration() {


        String headers = config.getProperty(ConfigNames.EXCLUDED_HTTP_HEADERS, "");
        for (String header : headers.split(",")) {
            header = header.trim();
            if (!header.isEmpty())
                excludedHttpHeaders.add(header);
        }


    }



//    private boolean proxifyXMLHttpRequest = true;
//    private final String _xhrWrapperScript;

//    private boolean proxifySWF = false; //todo

//    private String bannedImageUrlPatterns; //todo
    /* If an image is banned, then replace it with a 1x1 transparent GIF to show blank space instead of a broken image icon. */
//    private boolean returnEmptyGIF = true; //todo

    public boolean isHttpHeaderExcluded(String headerName) {
        return excludedHttpHeaders.contains(headerName);
    }

}
