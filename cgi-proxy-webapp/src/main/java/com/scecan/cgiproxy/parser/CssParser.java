package com.scecan.cgiproxy.parser;

import com.scecan.cgiproxy.util.IOUtils;
import com.scecan.cgiproxy.util.URLProxifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sandu Cecan
 */
public class CssParser extends ResponseParser {

    private static final Logger logger = LoggerFactory.getLogger(CssParser.class);

    private static final Pattern CSS_URL_PATTERN = Pattern.compile("(url\\([\"|']?)(.*?)([\"|']?\\))");

    protected CssParser(String charset) {
        super(charset);
    }

    @Override
    public InputStream parse(InputStream inputStream, URLProxifier urlProxifier) throws IOException {
        logger.debug("Parsing CSS");
        String inputCss = IOUtils.toString(inputStream, charset);
        logger.trace("Received CSS:\n{}", inputCss);
        String outputCss = proxifyCssUrls(inputCss, urlProxifier);
        logger.trace("Parsed CSS:\n{}", outputCss);
        return IOUtils.toInputStream(outputCss, charset);
    }

    /**
     * Proxifies all URLs from the CSS.
     *
     * @param css the CSS to proxify
     * @param urlProxifier the implementation used to proxify all URLs in CSS rules
     * @return CSS with all URLs in it proxified
     */
    public static String proxifyCssUrls(String css, URLProxifier urlProxifier) {
        Matcher matcher = CSS_URL_PATTERN.matcher(css);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String urlToProxify = matcher.group(2);
            String proxifiedURL = urlProxifier.proxify(urlToProxify);
            logger.trace("Changed '{}' url with '{}' url", urlToProxify, proxifiedURL);
            matcher.appendReplacement(buffer, "$1" + proxifiedURL.replace("$","\\$") + "$3");
        }
        matcher.appendTail(buffer);
        String refactoredCss = buffer.toString();
        return refactoredCss;
    }
}
