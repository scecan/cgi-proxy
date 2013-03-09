package com.scecan.cgiproxy.servlet;

import com.scecan.cgiproxy.services.CGIProxyService;
import com.scecan.cgiproxy.util.CGIProxifier;
import com.scecan.cgiproxy.util.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sandu Cecan
 */
public class CGIProxyServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CGIProxyServlet.class);


    private final static Set<String> VALID_PROTOCOLS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList("http", "https"))
    );

    public static final String URL_TO_PROXIFY_PARAM = "urlToProxify";
    
    @Override
    public void init() throws ServletException {
        logger.debug("Initialize {} servlet.", CGIProxyServlet.class.getName());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.debug("Proxify: {} - {}{}", new String[] {
                request.getMethod(),
                request.getRequestURL().toString(),
                request.getQueryString() != null ? ("?"+request.getQueryString()) : ""
        });
        if (request.getPathInfo() == null || "/".equals(request.getPathInfo())) {
            if ("GET".equals(request.getMethod())) {
                // get the URL to proxify and send redirect to proxify requested URL
                String urlToProxify = request.getParameter(URL_TO_PROXIFY_PARAM);
                logger.debug("Requested URL to proxify is: {}", urlToProxify);
                if (urlToProxify != null || !urlToProxify.trim().isEmpty()) {
                    String redirectPathInfo = createRedirectPathInfo(urlToProxify);
                    response.sendRedirect(request.getContextPath()+request.getServletPath()+redirectPathInfo);
                    return;
                }
            }
        } else {
            // proxify requested URL
            URL requestedURL = getRequestedURL(request.getPathInfo(), request.getQueryString());
            CGIProxifier cgiProxifier = new CGIProxifier(request, response, requestedURL);
            cgiProxifier.proxify();
            return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Incorrect parameters where provided.");
    }
    
    protected static String createRedirectPathInfo(String urlToProxifyParam) throws MalformedURLException {
        String url = urlToProxifyParam.trim();
        if (!urlToProxifyParam.toLowerCase().startsWith("http://")
                && !urlToProxifyParam.toLowerCase().startsWith("https://"))
            url = "http://"+url;
        URL urlToProxify = new URL(url);
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(urlToProxify.getProtocol()).append("/").append(urlToProxify.getHost());
        if (urlToProxify.getPort() != -1)
            sb.append(":").append(urlToProxify.getPort());
        sb.append(urlToProxify.getFile());
        return sb.toString();
    }

    protected static URL getRequestedURL(String pathInfo, String queryString) throws MalformedURLException {
        String path[] = pathInfo.split("/");
        // protocol/host/port/...
        if (path.length >= 3 ) {
            String protocol = path[1];
            if (!VALID_PROTOCOLS.contains(protocol))
                throw new MalformedURLException(String.format("Valid protocols are: %s. Passed protocol is: %s", VALID_PROTOCOLS, protocol));
            String host = path[2];
            int port = -1;
            int index = host.indexOf(":");
            if (index != -1) {
                String portValue = host.substring(index + 1);
                try {
                    port = Integer.parseInt(portValue);
                } catch (NumberFormatException e) {
                    throw new MalformedURLException(String.format("Port '%s' is not a number.", portValue));
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
            throw new MalformedURLException(String.format("Could not create an URL from '%s' string.", pathInfo));
        }
    }

}
