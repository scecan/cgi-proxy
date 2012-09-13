package com.scecan.cgiproxy.guice;

import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.scecan.cgiproxy.servlet.CGIProxyServlet;
import com.scecan.cgiproxy.servlet.URLHandlerServlet;
import com.scecan.cgiproxy.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.net.URL;

/**
 * @author Sandu Cecan
 */
public class CGIProxyModule extends ServletModule {

    private static final Logger logger = LoggerFactory.getLogger(CGIProxyModule.class);

    private static final String GUICE_FILTER_PATH_PARAM = "guiceFilterPathPrefix";

    private static final String PROXIFIER_URL_MAPPING = "/proxify";
    private static final String PROXY_URL_MAPPING = "/proxy/*";


    @Override
    protected void configureServlets() {

        ServletContext context = getServletContext();
        String contextPath = context.getContextPath();
        String guiceFilterPath = context.getInitParameter(GUICE_FILTER_PATH_PARAM);
        if (guiceFilterPath == null) {
            throw new NullPointerException("'"+GUICE_FILTER_PATH_PARAM+"' init parameter should not be null");
        }



        String proxyPath = (contextPath + guiceFilterPath + PROXY_URL_MAPPING.replace("/*", "")).replace("//", "/");

        bind(String.class)
                .annotatedWith(Names.named(Configuration.PROXY_PATH))
                .toInstance(proxyPath);

        String proxifierUrlMapping = (guiceFilterPath+PROXIFIER_URL_MAPPING).replace("//", "/");
        serve( proxifierUrlMapping )
                .with(URLHandlerServlet.class);
        logger.debug("Serve '" + proxifierUrlMapping + "' url with '" + URLHandlerServlet.class.getName() + "' servlet.");
        String proxyUrlMapping = (guiceFilterPath+PROXY_URL_MAPPING).replace("//", "/");
        serve( proxyUrlMapping )
                .with(CGIProxyServlet.class);
        logger.debug("Serve '" + proxyUrlMapping + "' url with '" + CGIProxyServlet.class.getName() + "' servlet.");



    }


}
