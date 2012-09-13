package com.scecan.cgiproxy.util;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static junit.framework.Assert.assertEquals;

/**
 * @author Sandu Cecan
 */
public class URLBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullParams() throws MalformedURLException {
        URL url = URLBuilder.getRequestedURL(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidParams() throws MalformedURLException {
        URL url = URLBuilder.getRequestedURL("invalid/path", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidProtocol() throws MalformedURLException {
        URL url = URLBuilder.getRequestedURL("/invalidProtocol/localhost/-1/index.html", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPort() throws MalformedURLException {
        URL url = URLBuilder.getRequestedURL("/http/localhost:invalidPort/index.html", null);
    }

    @Test
    public void testValidPath() throws MalformedURLException {
        URL url = URLBuilder.getRequestedURL("/http/localhost/index.html", null);
        assertEquals("http://localhost/index.html", url.toString());

        URL url2 = URLBuilder.getRequestedURL("/https/localhost/test/index.html", null);
        assertEquals("https://localhost/test/index.html", url2.toString());

        URL url3 = URLBuilder.getRequestedURL("/https/localhost/", null);
        assertEquals("https://localhost", url3.toString());

        URL url4 = URLBuilder.getRequestedURL("/https/localhost:8080/", null);
        assertEquals("https://localhost:8080", url4.toString());
    }

    @Test
    public void testValidPathAndQuery() throws MalformedURLException {
        URL url = URLBuilder.getRequestedURL("/http/localhost/index.html", "param=value");
        assertEquals("http://localhost/index.html?param=value", url.toString());

        URL url2 = URLBuilder.getRequestedURL("/https/localhost:8080", "param=value");
        assertEquals("https://localhost:8080?param=value", url2.toString());

        URL url3 = URLBuilder.getRequestedURL("/https/localhost/", "param=value");
        assertEquals("https://localhost?param=value", url3.toString());
    }

}
