package com.scecan.cgiproxy.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sandu Cecan
 */
public class URLBuilder {

    private final static Set<String> VALID_PROTOCOLS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList("http", "https"))
    );

    public static URL getRequestedURL(String pathInfo, String queryString) throws MalformedURLException {
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
                return new URL(protocol, host, port, file);
            } else {
                throw new IllegalArgumentException("");//todo message
            }
        } else {
            throw new IllegalArgumentException("");//todo message
        }
    }


    public static String proxifyURL(String urlToProxify, String proxyPath, URL hostURL) {
        String refactoredUrl = urlToProxify;
        if (urlToProxify != null) {
            if ( urlToProxify.startsWith("http://") ) {
                refactoredUrl = createRelativeUrl(proxyPath, "http", urlToProxify.substring("http://".length()));
            } else if (urlToProxify.startsWith("https://")) {
                refactoredUrl = createRelativeUrl(proxyPath, "https", urlToProxify.substring("https://".length()));
            } else if (urlToProxify.startsWith("//")) {
                refactoredUrl = createRelativeUrl(proxyPath, hostURL.getProtocol(), urlToProxify.substring("//".length()));
            } else if (urlToProxify.startsWith("/")) {
                refactoredUrl = proxyPath + "/" + hostURL.getProtocol() + "/" + hostURL.getHost() +
                        ((hostURL.getPort() != -1) ? (":" + hostURL.getPort()) : "") + urlToProxify;
            }
        }
        return refactoredUrl;
    }

    private static String createRelativeUrl(String prefixPath, String protocol, String url) {
        return prefixPath + "/" + protocol + "/" + url;
    }

}
