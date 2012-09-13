package com.scecan.cgiproxy.parser;

import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * @author Sandu Cecan
 */
public class ResponseParserTest {

    @Test
    public void testFactoryMethod() {

        ResponseParser p = ResponseParser.createParser(null);
        assertNull(p);

        ResponseParser p1 = ResponseParser.createParser("unkownContentType");
        assertNull(p1);

        ResponseParser p2 = ResponseParser.createParser("text/html");
        assertEquals(HtmlParser.class, p2.getClass());
        assertNull(p2.getCharset());

        ResponseParser p3 = ResponseParser.createParser("text/html; charset=UTF-8");
        assertEquals(HtmlParser.class, p3.getClass());
        assertEquals("UTF-8", p3.getCharset());

        ResponseParser p4 = ResponseParser.createParser("text/css; charset=UTF-8");
        assertEquals(CssParser.class, p4.getClass());
        assertEquals("UTF-8", p4.getCharset());

    }

}
