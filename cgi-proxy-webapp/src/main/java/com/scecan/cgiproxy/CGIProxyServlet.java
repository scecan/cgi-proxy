package com.scecan.cgiproxy;

import com.scecan.cgiproxy.services.CGIProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CGIProxyServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CGIProxyServlet.class);
    
    private static final String URL_PATTERN_PARAM = "url-pattern";

    private CGIProxyService cgiProxyService;

    @Override
    public void init() throws ServletException {

        logger.info("Initialize CGIProxyServlet");
        
        String contextPath = getServletContext().getContextPath();
        String urlPattern = this.getInitParameter(URL_PATTERN_PARAM);

        cgiProxyService = cgiProxyService.initializeGAEProxyService(contextPath, urlPattern);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //todo maybe to implement "getLastModified()"
        if (logger.isDebugEnabled()) {
            logger.debug("Request -> " + request.getMethod() + ": " + request.getRequestURI() +
                    (request.getQueryString() != null ? ("?" + request.getQueryString()) : ""));
        }
        cgiProxyService.forwardRequest(request, response);





/*
        HTTPMethod method = HTTPMethod.valueOf(request.getMethod());
        System.out.println("Resolved Method: "+method);
        HTTPRequest httpRequest = new HTTPRequest(url, method, fetchOptions);
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            httpRequest.setHeader(new HTTPHeader(name, value));
        }

        HTTPResponse httpResponse = urlFetchService.fetch(httpRequest);

        System.out.println("Response code: " + httpResponse.getResponseCode());

        for (HTTPHeader header : httpResponse.getHeaders() ) {
            response.setHeader(header.getName(), header.getValue());
        }
        byte[] content = httpResponse.getContent();

        response.getOutputStream().write(content);
        response.getOutputStream().flush();*/
        
        
    }

}
