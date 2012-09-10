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
        if (logger.isDebugEnabled()) {
            logger.debug("Request -> " + request.getMethod() + ": " + request.getRequestURI() +
                    (request.getQueryString() != null ? ("?" + request.getQueryString()) : ""));
        }
        cgiProxyService.forwardRequest(request, response);

    }

}
