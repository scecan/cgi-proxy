package com.scecan.cgiproxy.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sandu Cecan
 */
public class RequestURL {

    private final static List<String> VALID_PROTOCOLS = Arrays.asList("http", "https");

    private final String protocol;
    private final String host;
    private final int port;
    private final String file;

    private final String prefixPath;

    public RequestURL(String protocol, String host, int port, String file, String prefixPath) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.file = file;
        this.prefixPath = prefixPath;
    }

    public static RequestURL build(String pathInfo, String queryString, String prefixPath) {
        if (pathInfo != null) {
            String path[] = pathInfo.split("/");
            // protocol/host/port/...
            if (path.length >= 3 ) {
                String protocol = path[1];
                if (!VALID_PROTOCOLS.contains(protocol))
                    throw new IllegalArgumentException(""); //todo message
                String host = path[2];
                int port = -1;
                int index = host.indexOf(":");
                if (index != -1) {
                    try {
                        port = Integer.parseInt(host.substring(index+1));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("", e);//todo message
                    }
                    host = host.substring(0, index);
                }
                StringBuilder sb = new StringBuilder();
                for ( int i = 3; i < path.length; i ++ ) {
                    sb.append("/");
                    sb.append(path[i]);
                }
                if (queryString != null) {
                    sb.append("?");
                    sb.append(queryString);
                }
                String file = sb.toString();
                return new RequestURL(protocol, host, port, file, prefixPath);
            } else {
                throw new IllegalArgumentException("");//todo message
            }
        } else {
            throw new IllegalArgumentException("");//todo message
        }
    }

    public URL createURL() throws MalformedURLException {
        return new URL(protocol, host, port, file);
    }


    public String refactorUrl(String url) {
        String refactoredUrl = url;
        if (url != null) {
            if ( url.startsWith("http://") ) {
                refactoredUrl = createRelativeUrl("http", url.substring("http://".length()));
            } else if (url.startsWith("https://")) {
                refactoredUrl = createRelativeUrl("https", url.substring("https://".length()));
            } else if (url.startsWith("//")) {
                refactoredUrl = createRelativeUrl(protocol, url.substring("//".length()));
            } else if (url.startsWith("/")) {
                refactoredUrl = prefixPath + protocol + "/" + host + ((port != -1) ? (":"+port) : "") + url;
            }
        }
        return refactoredUrl;
    }
    
    private String createRelativeUrl(String protocol, String url) {
        return prefixPath + protocol + "/" + url;
    }
}
