package com.scecan.cgiproxy.util;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static junit.framework.Assert.assertEquals;

/**
 * @author Sandu Cecan
 */
public class RequestURLTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullParams() {
        RequestURL requestURL = RequestURL.build(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidParams() {
        RequestURL requestURL = RequestURL.build("invalid/path", null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidProtocol() {
        RequestURL requestURL = RequestURL.build("/invalidProtocol/localhost/-1/index.html", null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPort() {
        RequestURL requestURL = RequestURL.build("/http/localhost:invalidPort/index.html", null, null);
    }

    @Test
    public void testValidPath() throws MalformedURLException {
        RequestURL requestURL = RequestURL.build("/http/localhost/index.html", null, null);
        URL url = requestURL.createURL();
        assertEquals("http://localhost/index.html", url.toString());

        RequestURL requestURL2 = RequestURL.build("/https/localhost/test/index.html", null, null);
        URL url2 = requestURL2.createURL();
        assertEquals("https://localhost/test/index.html", url2.toString());

        RequestURL requestURL3 = RequestURL.build("/https/localhost/", null, null);
        URL url3 = requestURL3.createURL();
        assertEquals("https://localhost", url3.toString());

        RequestURL requestURL4 = RequestURL.build("/https/localhost:8080/", null, null);
        URL url4 = requestURL4.createURL();
        assertEquals("https://localhost:8080", url4.toString());
    }

    @Test
    public void testValidPathAndQuery() throws MalformedURLException {
        RequestURL requestURL = RequestURL.build("/http/localhost/index.html", "param=value", null);
        URL url = requestURL.createURL();
        assertEquals("http://localhost/index.html?param=value", url.toString());

        RequestURL requestURL2 = RequestURL.build("/https/localhost:8080", "param=value", null);
        URL url2 = requestURL2.createURL();
        assertEquals("https://localhost:8080?param=value", url2.toString());

        RequestURL requestURL3 = RequestURL.build("/https/localhost/", "param=value", null);
        URL url3 = requestURL3.createURL();
        assertEquals("https://localhost?param=value", url3.toString());
    }

}
