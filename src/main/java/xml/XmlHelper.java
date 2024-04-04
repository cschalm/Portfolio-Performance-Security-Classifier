package xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class XmlHelper {

    public Document readXmlStream(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        return builder.parse(stream);
    }

    public Document readXmlStream(String fileName) throws IOException, ParserConfigurationException, SAXException {
        return this.readXmlStream(Files.newInputStream(new File(fileName).toPath()));
    }

    public Node getFirstChild(Element parentElement, String tagName) {
        return parentElement.getElementsByTagName(tagName).item(0);
    }

    public String getTextContent(Element node, String tagName) {
        Node firstChild = getFirstChild(node, tagName);

        return firstChild == null ? "" : firstChild.getTextContent();
    }

    public Element getFirstChildElementWithNodeName(Node node, String nodeName) {
        Element result = null;
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE && children.item(i).getNodeName().equals(nodeName)) {
                result = (Element) children.item(i);
            }
        }

        return result;
    }

}
