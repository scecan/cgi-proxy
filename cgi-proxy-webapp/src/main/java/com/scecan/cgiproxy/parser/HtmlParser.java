package com.scecan.cgiproxy.parser;

import com.scecan.cgiproxy.util.IOUtils;
import com.scecan.cgiproxy.util.URLBuilder;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.*;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;

/**
 * @author Sandu Cecan
 */
public class HtmlParser extends ResponseParser {

    private static final Logger logger = LoggerFactory.getLogger(HtmlParser.class);
    
    public HtmlParser(String charset) {
        super(charset);
    }

    @Override
    public InputStream parse(InputStream inputStream , String proxyPath, URL hostURL) throws IOException {
        logger.debug("Parsing HTML");
        String inputHtml = IOUtils.toString(inputStream, charset);
        logger.trace("Received HTML:\n{}", inputHtml);
        String outputHtml = inputHtml;
        Parser parser = Parser.createParser(inputHtml, null);
        try {
            NodeList root = parser.parse(null);

            root.visitAllNodesWith(new HtmlProxifier(proxyPath, hostURL));
            outputHtml = root.toHtml();
            logger.trace("Parsed HTML:\n{}", inputHtml);
        } catch (ParserException e) {
            logger.warn("Exception while parsing the HTML. Original content will be forwarded.", e);
        }
        return IOUtils.toInputStream(outputHtml, charset);
    }

    private static class HtmlProxifier extends NodeVisitor {

        private static final String XHR_PROXY_SCRIPT;
        static {
            String temp;
            try {
                temp  = IOUtils.toString(HtmlProxifier.class.getResourceAsStream("/xmlHttpRequestWrapper.jstemplate"), null);
            } catch (IOException e) {
                logger.warn("Could not read the js template.", e);
                temp = "";
            }
            XHR_PROXY_SCRIPT = temp;
        }

        private final String proxyPath;
        private final URL hostURL;

        public HtmlProxifier(String proxyPath, URL hostURL) {
            this.proxyPath = proxyPath;
            this.hostURL = hostURL;
        }

        @Override
        public void visitTag(Tag tag) {

            if (tag instanceof LinkTag) {
                LinkTag linkTag = (LinkTag) tag;
                String url = linkTag.getLink();
                if (url != null)
                    linkTag.setLink( URLBuilder.proxifyURL(url, proxyPath, hostURL) );
            } else if(tag instanceof ImageTag) {
                ImageTag imageTag = (ImageTag) tag;
                String url = imageTag.getImageURL();
                if (url != null)
                    imageTag.setImageURL( URLBuilder.proxifyURL(url, proxyPath, hostURL) );
            } else if (tag instanceof ScriptTag) {
                ScriptTag scriptTag = (ScriptTag) tag;
                String url = scriptTag.getAttribute("src");
                if (url != null)
                    scriptTag.setAttribute("src", URLBuilder.proxifyURL(url, proxyPath, hostURL) );
            } else if (tag instanceof FrameTag) {
                FrameTag frameTag = (FrameTag) tag;
                String url = frameTag.getFrameLocation();
                if (url != null)
                    frameTag.setFrameLocation( URLBuilder.proxifyURL(url, proxyPath, hostURL) );
            } else if (tag instanceof FormTag) {
                FormTag formTag = (FormTag) tag;
                String url = formTag.getFormLocation();
                if (url != null) {
                    formTag.setFormLocation( URLBuilder.proxifyURL(url, proxyPath, hostURL) );
                }
            } else if (tag instanceof ObjectTag) {
                //todo
            } else if (tag instanceof AppletTag) {
                //todo
            } else if (tag instanceof HeadTag) {
                ScriptTag scriptTag = new ScriptTag();
                scriptTag.setType("text/javascript");
                scriptTag.setScriptCode(String.format(XHR_PROXY_SCRIPT, proxyPath, hostURL.getProtocol(), hostURL.getHost(), hostURL.getPort()));
                TagNode endTag = new TagNode();
                endTag.setTagName("/SCRIPT");
                scriptTag.setEndTag(endTag);

                NodeList list = new NodeList();

                NodeList children = tag.getChildren();
                boolean scriptInserted = false;
                for (int i = 0; i < children.size(); i++) {
                    Node child = children.elementAt(i);
                    if (child instanceof Tag)
                        if ("script".equalsIgnoreCase(((Tag) child).getTagName()))
                            if (!scriptInserted) {
                                list.add(scriptTag);
                                scriptInserted = true;
                            }
                    list.add(child);
                }
                if (!scriptInserted) {
                    list.add(scriptTag);
                    scriptInserted = true;
                }
                tag.setChildren(list);
            } else if ("link".equalsIgnoreCase(tag.getTagName())) {
                String url = tag.getAttribute("href");
                if (url != null) {
                    tag.setAttribute("href", URLBuilder.proxifyURL(url, proxyPath, hostURL));
                }
            } else if ("iframe".equalsIgnoreCase(tag.getTagName())) {
                String url = tag.getAttribute("src");
                if (url != null) {
                    tag.setAttribute("src", URLBuilder.proxifyURL(url, proxyPath, hostURL));
                }
            }
        }

        /*@Override
        public void visitStringNode(Text string) {
            if (string.getParent() instanceof Tag) {
                String parentTagName = ((Tag)string.getParent()).getTagName();
                if ("style".equalsIgnoreCase(parentTagName)) {
                    string.setText(CssParser.proxifyCssUrls(string.getText(), requestURL));
                }
            }
        }*/

    }

}
