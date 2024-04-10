package xml;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.logging.Logger;

public class XmlFileReader {
    private static final Logger logger = Logger.getLogger(XmlFileReader.class.getCanonicalName());

    public NodeList getAllSecurities(Document doc) {
        try {
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            return doc.getElementsByTagName("security");
        } catch (Exception e) {
            logger.warning("Error selecting all security-elements from document: " + e.getMessage());
        }
        return null;
    }

}
