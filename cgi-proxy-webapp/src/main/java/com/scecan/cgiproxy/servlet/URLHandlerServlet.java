package com.scecan.cgiproxy.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.scecan.cgiproxy.guice.Constants;
import com.scecan.cgiproxy.util.URLBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

/**
 * @author Sandu Cecan
 */
@Singleton
public class URLHandlerServlet extends HttpServlet {

    protected final String proxyPath;

    @Inject
    public URLHandlerServlet(@Named(Constants.PROXY_PATH_ANNOTATION) String proxyPath) {
        this.proxyPath = proxyPath;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String submittedUrl = request.getParameter("url");
        if (submittedUrl == null || submittedUrl.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Submitted URL is not found.");
        } else {
            if (!submittedUrl.startsWith("http://") && !submittedUrl.startsWith("https://")) {
                submittedUrl = "http://" + submittedUrl;
            }
            URL urlToProxify = new URL(submittedUrl);

            String proxifiedURL = URLBuilder.proxifyURL(submittedUrl, proxyPath, null);

            response.sendRedirect(proxifiedURL);
        }


    }
}
