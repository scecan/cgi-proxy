package com.scecan.cgiproxy.parser;

import com.scecan.cgiproxy.util.IOUtils;
import com.scecan.cgiproxy.util.RequestURL;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.*;
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

            root.visitAllNodesWith(new HtmlNodesVisitor(requestURL));
            outputHtml = root.toHtml();
            logger.trace("Parsed HTML:\n{}", inputHtml);
        } catch (ParserException e) {
            logger.warn("Exception while parsing the HTML. Original content will be forwarded.", e);
        }
        return IOUtils.toInputStream(outputHtml, charset);
    }

    private static class HtmlNodesVisitor extends NodeVisitor {

        private static final String XHR_PROXY_SCRIPT;
        static {
            String XHR_PROXY_SCRIPT1;
            try {
                String script = IOUtils.toString(HtmlNodesVisitor.class.getResourceAsStream("/xmlHttpRequestWrapper.js"), null);
                XHR_PROXY_SCRIPT1 = script;
            } catch (IOException e) {
                e.printStackTrace();
                XHR_PROXY_SCRIPT1 = "";
            }
            XHR_PROXY_SCRIPT = XHR_PROXY_SCRIPT1;
        }

        private final RequestURL requestURL;

        public HtmlNodesVisitor(RequestURL requestURL) {
            this.requestURL = requestURL;
        }

        @Override
        public void visitTag(Tag tag) {

            if (tag instanceof LinkTag) {
                LinkTag linkTag = (LinkTag) tag;
                String url = linkTag.getLink();
                if (url != null)
                    linkTag.setLink( requestURL.refactorUrl(url) );
            } else if(tag instanceof ImageTag) {
                ImageTag imageTag = (ImageTag) tag;
                String url = imageTag.getImageURL();
                if (url != null)
                    imageTag.setImageURL( requestURL.refactorUrl(url) );
            } else if (tag instanceof ScriptTag) {
                ScriptTag scriptTag = (ScriptTag) tag;
                String url = scriptTag.getAttribute("src");
                if (url != null)
                    scriptTag.setAttribute("src", requestURL.refactorUrl(url) );
            } else if (tag instanceof FrameTag) {
                FrameTag frameTag = (FrameTag) tag;
                String url = frameTag.getFrameLocation();
                if (url != null)
                    frameTag.setFrameLocation( requestURL.refactorUrl(url) );
            } else if (tag instanceof FormTag) {
                FormTag formTag = (FormTag) tag;
                String url = formTag.getFormLocation();
                if (url != null) {
                    formTag.setFormLocation( requestURL.refactorUrl(url) );
                }
            } else if (tag instanceof ObjectTag) {
                //todo
            } else if (tag instanceof AppletTag) {
                //todo
            } /*else if (tag instanceof HeadTag) {
                ScriptTag scriptTag = new ScriptTag();
                scriptTag.setType("text/javascript");
                scriptTag.setScriptCode(XHR_PROXY_SCRIPT);
                TagNode endTag = new TagNode();
                endTag.setTagName("/SCRIPT");
                scriptTag.setEndTag(endTag);

                NodeList list = new NodeList(scriptTag);
                list.add(tag.getChildren());
                tag.setChildren(list);
            }*/ else if ("link".equalsIgnoreCase(tag.getTagName())) {
                String url = tag.getAttribute("href");
                if (url != null) {
                    tag.setAttribute("href", requestURL.refactorUrl(url));
                }
            } else if ("iframe".equalsIgnoreCase(tag.getTagName())) {
                String url = tag.getAttribute("src");
                if (url != null) {
                    tag.setAttribute("src", requestURL.refactorUrl(url));
                }
            }

            /*if ( !scriptAppended && tag instanceof HeadTag ) {
                ScriptTag scriptTag = new ScriptTag();
                scriptTag.setType("text/javascript");
                scriptTag.setScriptCode("alert('hello world!');");
                scriptTag.setEndTag(new ScriptTag());

                NodeList list = new NodeList(scriptTag);
                list.add(tag.getChildren());
                tag.setChildren(list);
                scriptAppended = true;
            }*/

            /*
            for (String[] entry : tagsToRefactor) {
                if (entry[0].equalsIgnoreCase(tag.getTagName())) {
                    refactorAttributeUrl(tag, entry[1]);
                    break;
                }
            }*/
            //todo meta tag ???
            //todo object tag ???
        }

        /*@Override
        public void visitStringNode(Text string) {
            if (string.getParent() instanceof Tag) {
                String parentTagName = ((Tag)string.getParent()).getTagName();
                if ("style".equalsIgnoreCase(parentTagName)) {
                    string.setText(CssParser.refactorCssUrls(string.getText(), requestURL));
                }
            }
        }*/


        private void refactorAttributeUrl(Tag tag, String attributeName) {
            String url = tag.getAttribute(attributeName);
            if (url != null) {
                String refactoredUrl = requestURL.refactorUrl(url);
                tag.setAttribute(attributeName, refactoredUrl);
                logger.trace("Changed '{}' attribute of '{}' tag ('{}' -> '{}')", new String[]{attributeName, tag.getTagName(), url, refactoredUrl});
            }
        }
    }

}
