package com.scecan.cgiproxy.services;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.scecan.cgiproxy.guice.Constants;
import com.scecan.cgiproxy.util.Configuration;
import com.scecan.cgiproxy.util.ContentDecoder;
import com.scecan.cgiproxy.parser.ResponseParser;
import com.scecan.cgiproxy.util.IOUtils;
import com.scecan.cgiproxy.util.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @author Sandu Cecan
 */
@Singleton
public class CGIProxyService {

    private static final Logger logger = LoggerFactory.getLogger(CGIProxyService.class);

    /**
     * Those headers should be ignored in the response because they are set by the servlet container.
     */
    private static final Set<String> HEADERS_HANDLED_BY_SERVLET_CONTAINER = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
                    "Transfer-Encoding",
                    "Content-Encoding",
                    "Content-Length"
            )));

    private static final String LOCATION_HEADER_NAME = "Location";

    /**
     * The path to the proxy servlet
     */
    private final String proxyPath;
    private final Provider<Configuration> configurationProvider;

    @Inject
    private CGIProxyService(@Named(Constants.PROXY_PATH_ANNOTATION) String proxyPath,
                            Provider<Configuration> configurationProvider) {
        this.proxyPath = proxyPath;
        this.configurationProvider = configurationProvider;
        logger.info("Initialized CGIProxyService with the proxyPath='{}'", this.proxyPath);
    }

    public void proxifyRequestedURL(HttpServletRequest request, HttpServletResponse response, URL requestedURL) throws ServletException, IOException {
        HttpURLConnection connection = initHttpConnection(requestedURL);

        String method = request.getMethod();
        connection.setRequestMethod(method);

        setRequestHeaders(request, connection);

        if (!"GET".equals(method)) {
            connection.setDoOutput(true);
            sendBodyToConnection(request, connection);
        }
        logger.debug("Response code: {}", connection.getResponseCode());
        response.setStatus(connection.getResponseCode());
        // todo handle error codes
        setResponseHeaders(connection, response, requestedURL);
        sendBodyToResponse(connection, response, requestedURL);
    }

    private HttpURLConnection initHttpConnection(URL requestURL) throws IOException {
        logger.debug("Fetch {}", requestURL.toString());
        HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
        // configure the connection
        connection.setInstanceFollowRedirects(false); //let the browser to handle 30x redirects
        return connection;
    }

    private void setRequestHeaders(HttpServletRequest request, HttpURLConnection connection) {
        Configuration config = configurationProvider.get();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if ( ! config.isHttpHeaderExcluded(name) ) {
                Enumeration<String> headerValues = request.getHeaders(name);
                while (headerValues.hasMoreElements()) {
                    String value = headerValues.nextElement();
                    connection.addRequestProperty(name, value);
                    System.out.println("request: "+name+" = " + value);
                    logger.trace("Add request header -> {}: {}", name, value);
                }
            }
        }
    }

    private void sendBodyToConnection(HttpServletRequest request, HttpURLConnection connection) throws IOException {
        InputStream is = request.getInputStream();
        OutputStream os = connection.getOutputStream();
        IOUtils.pipe(is, os, new byte[100]);
    }

    private void setResponseHeaders(HttpURLConnection connection, HttpServletResponse response, URL requestedURL) {
        Map<String, List<String>> headersMap = connection.getHeaderFields();
        for (Map.Entry<String, List<String>> headerEntry : headersMap.entrySet()) {
            String name = headerEntry.getKey();
            for (String value : headerEntry.getValue()) {
                if (LOCATION_HEADER_NAME.equals(name)) {
                    // refactor domain specific headers
                    value = URLBuilder.proxifyURL(value, proxyPath, requestedURL);
                } else if(HEADERS_HANDLED_BY_SERVLET_CONTAINER.contains(name)) {
                    break; // ignore those headers, they are handled by the servlet container
                } else {
                    //todo handle cookies domain and path
                }
                response.setHeader(name, value);
                logger.trace("Add response header -> {}: {}", name, value);
            }
        }
    }

    private void sendBodyToResponse(HttpURLConnection connection, HttpServletResponse response, URL requestedURL) throws IOException {
        String contentEncodingValue = connection.getContentEncoding();
        String contentTypeValue = connection.getContentType();
        logger.debug("Content-Encoding: {}", contentEncodingValue);
        logger.debug("Content-Type: {}", contentTypeValue);
        // decode the InputStream
        InputStream decodedInputStream = ContentDecoder.decode(contentEncodingValue, connection.getInputStream());
        OutputStream outputStream = response.getOutputStream();
        // get the proper Parser for this Content-Type
        ResponseParser parser = ResponseParser.createParser(contentTypeValue);
        if (parser != null) { // if we have an parser for this 'Content-Type'
            // parse the content
            InputStream parsedInputStream = parser.parse(decodedInputStream, proxyPath, requestedURL);

            IOUtils.pipe(parsedInputStream, outputStream, new byte[1024]);
        } else {
            IOUtils.pipe(decodedInputStream, outputStream, new byte[1024]);
        }
    }




}
