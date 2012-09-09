package com.scecan.cgiproxy.parser;

import com.scecan.cgiproxy.util.IOUtils;
import com.scecan.cgiproxy.util.RequestURL;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author Sandu Cecan
 */
public class HtmlParser extends ResponseParser {

    private static final Logger logger = LoggerFactory.getLogger(HtmlParser.class);
    
    private static final String[][] tagsToRefactor = {
            // {tagName, attributeName}
            {"a","href"},
            {"link","href"},
            {"script","src"},
            {"iframe","src"},
            {"frame","src"},
            {"img","src"},
            {"form","action"}
    };

    private final RequestURL requestURL;

    public HtmlParser(String charset, RequestURL requestURL) {
        super(charset);
        this.requestURL = requestURL;
    }

    @Override
    public InputStream parse(InputStream inputStream) throws IOException {
        logger.debug("Parsing HTML");
        String inputHtml = IOUtils.toString(inputStream, charset);
        logger.trace("Received HTML:\n{}", inputHtml);
        String outputHtml = inputHtml;
        Parser parser = Parser.createParser(inputHtml, null);
        try {
            NodeList root = parser.parse(null);
            root.visitAllNodesWith(new NodeVisitor() {
                @Override
                public void visitTag(Tag tag) {
                    for (String[] entry : tagsToRefactor) {
                        if (entry[0].equalsIgnoreCase(tag.getTagName())) {
                            refactorAttributeUrl(tag, entry[1]);
                            break;
                        }
                    }
                    //todo meta tag ???
                    //todo object tag ???
                }

                @Override
                public void visitStringNode(Text string) {
                    if (string.getParent() instanceof Tag) {
                        String parentTagName = ((Tag)string.getParent()).getTagName();
                        if ("style".equalsIgnoreCase(parentTagName)) {
                            string.setText(CssParser.refactorCssUrls(string.getText(), requestURL));
                        }
                    }
                }
            });
            outputHtml = root.toHtml();
            logger.trace("Parsed HTML:\n{}", inputHtml);
        } catch (ParserException e) {
            logger.warn("Exception while parsing the HTML. Original content will be forwarded.", e);
        }
        return IOUtils.toInputStream(outputHtml, charset);
    }

    private void refactorAttributeUrl(Tag tag, String attributeName) {
        String url = tag.getAttribute(attributeName);
        if (url != null) {
            String refactoredUrl = requestURL.refactorUrl(url);
            tag.setAttribute(attributeName, refactoredUrl);
            logger.trace("Changed '{}' attribute of '{}' tag ('{}' -> '{}')", new String[]{attributeName, tag.getTagName(), url, refactoredUrl});
        }
    }

}
