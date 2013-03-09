package com.scecan.cgiproxy.util;

import com.scecan.cgiproxy.parser.ResponseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @author Sandu Cecan
 */
public class CGIProxifier {

    private static final Logger logger = LoggerFactory.getLogger(CGIProxifier.class);

    /**
     * Those headers should be ignored in the response because they are set by the servlet container.
     */
    private static final Set<String> HEADERS_HANDLED_BY_SERVLET_CONTAINER = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
                    "Transfer-Encoding",
                    "Content-Encoding",
                    "Content-Length"
            ))
    );

    private static final String LOCATION_HEADER_NAME = "Location";


    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final URL urlToProxify;

    private final URLProxifier urlProxifier;

    private final String httpMethod;

    private Configuration config;

    public CGIProxifier(HttpServletRequest request, HttpServletResponse response, URL urlToProxify) {
        this.request = request;
        this.response = response;
        this.urlToProxify = urlToProxify;

        httpMethod = request.getMethod();

        this.urlProxifier = new URLProxifier(request.getContextPath()+request.getServletPath(), urlToProxify);

        config = new Configuration(); //todo maybe session config
    }

    public void proxify() throws IOException {
        HttpURLConnection connection = instantiateConection();
        setRequestHeaders(connection);
        if (!"GET".equals(httpMethod)) {
            sendPostData(connection);
        }
        setResponseHeaders(connection);
        sendContentToResponse(connection);
    }

    private void setRequestHeaders(HttpURLConnection connection) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (name != null && !config.isHttpHeaderExcluded(name)) {
                Enumeration<String> headerValues = request.getHeaders(name);
                while (headerValues.hasMoreElements()) {
                    String value = headerValues.nextElement();
                    connection.addRequestProperty(name, value);
                    logger.trace("Add request header -> {}: {}", name, value);
                }
            }
        }
    }

    private HttpURLConnection instantiateConection() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) urlToProxify.openConnection();
        // configure the connection
        connection.setInstanceFollowRedirects(false); //let the browser to handle 30x redirects
        connection.setRequestMethod(httpMethod);
        return connection;
    }

    private void sendPostData(HttpURLConnection connection) throws IOException {
        InputStream is = request.getInputStream();
        OutputStream os = connection.getOutputStream();
        IOUtils.pipe(is, os, new byte[100]);
    }

    private void setResponseHeaders(HttpURLConnection connection) throws IOException {
        Map<String, List<String>> headersMap = connection.getHeaderFields();
        for (Map.Entry<String, List<String>> headerEntry : headersMap.entrySet()) {
            String name = headerEntry.getKey();
            if (name != null) {
                for (String value : headerEntry.getValue()) {
                    if (LOCATION_HEADER_NAME.equals(name)) {
                        // refactor domain specific headers
                        value = urlProxifier.proxify(value);
                    } else if (HEADERS_HANDLED_BY_SERVLET_CONTAINER.contains(name)) {
                        break; // ignore those headers, they are handled by the servlet container
                    } else {
                        //todo handle cookies domain and path
                    }
                    response.addHeader(name, value);
                    logger.trace("Add response header -> {}: {}", name, value);
                }
            }
        }
        logger.debug("Response code: {}", connection.getResponseCode());
        response.setStatus(connection.getResponseCode());
    }

    private void sendContentToResponse(HttpURLConnection connection) throws IOException {
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
            InputStream parsedInputStream = parser.parse(decodedInputStream, urlProxifier);

            IOUtils.pipe(parsedInputStream, outputStream, new byte[1024]);
        } else {
            IOUtils.pipe(decodedInputStream, outputStream, new byte[1024]);
        }
    }


}
