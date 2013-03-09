package com.scecan.cgiproxy.util;

import java.net.URL;

/**
 * @author Sandu Cecan
 */
public class URLProxifier {
    
    
    private final String proxyServletPath;
    private final URL hostURL;

    public URLProxifier(String proxyServletPath, URL hostURL) {
        this.proxyServletPath = proxyServletPath;
        this.hostURL = hostURL;
    }

    public String proxify(String urlToProxify) {
        String proxifiedUrl = urlToProxify;
        if (urlToProxify != null) {
            if ( urlToProxify.startsWith("http://") ) {
                proxifiedUrl = createRelativeUrl("http", urlToProxify.substring("http://".length()));
            } else if (urlToProxify.startsWith("https://")) {
                proxifiedUrl = createRelativeUrl("https", urlToProxify.substring("https://".length()));
            } else if (urlToProxify.startsWith("//")) {
                proxifiedUrl = createRelativeUrl(hostURL.getProtocol(), urlToProxify.substring("//".length()));
            } else if (urlToProxify.startsWith("/")) {
                proxifiedUrl = createFullUrl(urlToProxify);
            }
        }
        return proxifiedUrl;
    }

    private String createRelativeUrl(String protocol, String url) {
        StringBuilder sb = new StringBuilder();
        sb.append(proxyServletPath).append("/").append(protocol).append("/").append(url);
        return sb.toString();
    }
    
    private String createFullUrl(String url) {
        StringBuilder sb = new StringBuilder(32);
        sb.append(proxyServletPath).append("/").append(hostURL.getProtocol()).append("/").append(hostURL.getHost());
        if (hostURL.getPort() != -1)
            sb.append(":").append(hostURL.getPort());
        sb.append(url);
        return sb.toString();
    }

}
