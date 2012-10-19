package com.scecan.cgiproxy.util;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.servlet.SessionScoped;
import com.scecan.cgiproxy.guice.Constants;
import com.scecan.cgiproxy.providers.RequestedUrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.*;

/**
 * @author Sandu Cecan
 */
@SessionScoped
public class Configuration implements Serializable {

    private static final long serialVersionUID = 1;

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private static final String CONFIG_FILE = "/config.properties";

    private static interface ConfigNames {
        static final String EXCLUDED_HTTP_HEADERS = "http.headers.excluded";
    }

    private static final Properties config = new Properties();
    static {
        try {
            config.load(Configuration.class.getResourceAsStream(CONFIG_FILE));
        } catch (IOException e) {
            logger.error("Could not load configuration file", e);
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

    private final String proxyPath;
    private final RequestedUrlProvider requestedUrlProvider;

    @Inject
    public Configuration(@Named(Constants.PROXY_PATH_ANNOTATION) String proxyPath,
                         RequestedUrlProvider requestedUrlProvider) {

        this.proxyPath = proxyPath;
        this.requestedUrlProvider = requestedUrlProvider;

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
        requestedUrlProvider.get();
        return excludedHttpHeaders.contains(headerName);
    }

}
