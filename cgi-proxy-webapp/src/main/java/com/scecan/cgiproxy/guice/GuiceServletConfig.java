package com.scecan.cgiproxy.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * @author Sandu Cecan
 */
public class GuiceServletConfig extends GuiceServletContextListener {
    @Override
    protected Injector getInjector() {
        //todo change the stage to "production"
        return Guice.createInjector(Stage.DEVELOPMENT, new CGIProxyModule());
//        return Guice.createInjector(Stage.PRODUCTION, new CGIProxyModule());
    }


}
