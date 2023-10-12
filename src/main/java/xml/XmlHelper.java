package xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class XmlHelper {

    public Document readXmlStream(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        return builder.parse(stream);
    }
    public Node getFirstChild(Element parentElement, String tagName) {
        return parentElement.getElementsByTagName(tagName).item(0);
    }

    public String getTextContent(Element node, String tagName) {
        Node firstChild = getFirstChild(node, tagName);

        return firstChild == null ? "" : firstChild.getTextContent();
    }

}
