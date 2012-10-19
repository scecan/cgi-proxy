package com.scecan.cgiproxy.servlet;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.scecan.cgiproxy.services.CGIProxyService;
import com.scecan.cgiproxy.util.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

@Singleton
public class CGIProxyServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CGIProxyServlet.class);


    private final CGIProxyService cgiProxyService;

    @Inject
    public CGIProxyServlet(CGIProxyService cgiProxyService) {
        this.cgiProxyService = cgiProxyService;
    }

    @Override
    public void init() throws ServletException {
        logger.info("Initialize CGIProxyServlet");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Request -> " + request.getMethod() + ": " + request.getRequestURI() +
                    (request.getQueryString() != null ? ("?" + request.getQueryString()) : ""));
        }

        URL requestedURL = URLBuilder.getRequestedURL(request.getPathInfo(), request.getQueryString());

        request.setAttribute(Key.get(URL.class, Names.named("requested-url")).toString(), requestedURL);

        cgiProxyService.proxifyRequestedURL(request, response, requestedURL);

    }

}
