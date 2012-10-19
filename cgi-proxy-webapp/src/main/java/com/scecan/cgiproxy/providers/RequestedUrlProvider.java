package com.scecan.cgiproxy.providers;

import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.net.URL;

/**
 * @author Sandu Cecan
 */
@RequestScoped
public class RequestedUrlProvider implements Provider<URL> {

    private final HttpServletRequest request;

    @Inject
    public RequestedUrlProvider(HttpServletRequest request) {
        this.request = request;
        System.out.println("constructor");

    }

    @Override
    public URL get() {

        System.out.println("get");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
