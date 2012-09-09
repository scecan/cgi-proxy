package com.scecan.cgiproxy.services;

import com.scecan.cgiproxy.util.ContentDecoder;
import com.scecan.cgiproxy.parser.ResponseParser;
import com.scecan.cgiproxy.util.IOUtils;
import com.scecan.cgiproxy.util.RequestURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * @author Sandu Cecan
 */
public class CGIProxyService {

    private static final Logger logger = LoggerFactory.getLogger(CGIProxyService.class);

    private static final List<String> HEADERS_HANDLED_BY_SERVLET_CONTAINER = Arrays.asList(
            "Transfer-Encoding",
            "Content-Encoding",
            "Content-Length"
    );

    private static final String LOCATION_HEADER_NAME = "Location";

    private static final List<String> EXCLUDED_HEADERS = Arrays.asList(
            "Content-Length",
            "Host",
            "Vary",
            "Via",
            "X-Forwarded-For",
            "X-ProxyUser-IP"
    );

    private final String prefixPath;

    private CGIProxyService(String prefixPath) {
        this.prefixPath = prefixPath;

        logger.info("Initialized CGIProxyService with the prefixPath='{}'", this.prefixPath);
    }

    public static CGIProxyService initializeGAEProxyService(String contextPath, String urlPattern) {
        logger.info("Initializing CGIProxyService (contextPath='{}' and urlPattern='{}'", contextPath, urlPattern);
        String path = contextPath.concat(urlPattern);
        int index = path.indexOf("*");
        if (index != -1 && index == path.length()-1) {
            String prefixPath = path.substring(0, index);
            return new CGIProxyService(prefixPath);
        } else {
            throw new IllegalArgumentException("url-pattern parameter not configured properly");
        }
    }


    public void forwardRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestURL requestURL = RequestURL.build(request.getPathInfo(), request.getQueryString(), prefixPath);
        HttpURLConnection connection = initHttpConnection(requestURL);

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
        setResponseHeaders(connection, response, requestURL);
        sendBodyToResponse(connection, response, requestURL);


    }

    private HttpURLConnection initHttpConnection(RequestURL requestURL) throws IOException {
        URL url = requestURL.createURL();
        logger.debug("Fetch {}", url.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // configure the connection
        connection.setInstanceFollowRedirects(false); //let the browser to handle 30x redirects
        return connection;
    }

    private void setRequestHeaders(HttpServletRequest request, HttpURLConnection connection) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if ( ! EXCLUDED_HEADERS.contains(name) ) {
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

    private void setResponseHeaders(HttpURLConnection connection, HttpServletResponse response, RequestURL requestURL) {
        Map<String, List<String>> headersMap = connection.getHeaderFields();
        for (Map.Entry<String, List<String>> headerEntry : headersMap.entrySet()) {
            String name = headerEntry.getKey();
            for (String value : headerEntry.getValue()) {
                if (LOCATION_HEADER_NAME.equals(name)) {
                    // refactor domain specific headers
                    value = requestURL.refactorUrl(value);
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

    private void sendBodyToResponse(HttpURLConnection connection, HttpServletResponse response, RequestURL requestURL) throws IOException {
        String contentEncodingValue = connection.getContentEncoding();
        String contentTypeValue = connection.getContentType();
        logger.debug("Content-Encoding: {}", contentEncodingValue);
        logger.debug("Content-Type: {}", contentTypeValue);
        // decode the InputStream
        InputStream decodedInputStream = ContentDecoder.decode(contentEncodingValue, connection.getInputStream());
        OutputStream outputStream = response.getOutputStream();
        // get the proper Parser for this Content-Type
        ResponseParser parser = ResponseParser.getParser(contentTypeValue, requestURL);
        if (parser != null) { // if we have an parser for this 'Content-Type'
            // parse the content
            InputStream parsedInputStream = parser.parse(decodedInputStream);

            IOUtils.pipe(parsedInputStream, outputStream, new byte[1024]);
        } else {
            IOUtils.pipe(decodedInputStream, outputStream, new byte[1024]);
        }
    }




}
