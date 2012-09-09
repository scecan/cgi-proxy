package com.scecan.cgiproxy.parser;

import com.scecan.cgiproxy.util.IOUtils;
import com.scecan.cgiproxy.util.RequestURL;
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

    private final RequestURL requestURL;

    protected CssParser(String charset, RequestURL requestURL) {
        super(charset);
        this.requestURL = requestURL;
    }

    @Override
    public InputStream parse(InputStream inputStream) throws IOException {
        logger.debug("Parsing CSS");
        String inputCss = IOUtils.toString(inputStream, charset);
        logger.trace("Received CSS:\n{}", inputCss);
        String outputCss = refactorCssUrls(inputCss, requestURL);
        logger.trace("Parsed CSS:\n{}", outputCss);
        return IOUtils.toInputStream(outputCss, charset);
    }
    
    public static String refactorCssUrls(String css, RequestURL requestURL) {
        Matcher matcher = CSS_URL_PATTERN.matcher(css);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String prefix = matcher.group(1);
            String url = matcher.group(2);
            String suffix = matcher.group(3);
            String refactoredUrl = requestURL.refactorUrl(url);
            logger.trace("Changed '{}' url with '{}' url", url, refactoredUrl);
            matcher.appendReplacement(buffer, prefix + refactoredUrl + suffix);
        }
        matcher.appendTail(buffer);
        String refactoredCss = buffer.toString();
        return refactoredCss;
    }
}
