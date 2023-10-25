package xml;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class XmlFileReader {
    public static Document getDocument(String filepath) {
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(filepath));
            normalizeDocument(doc);

            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void normalizeDocument(Document doc) {
        // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();
    }

    public static NodeList getAllSecurities(Document doc) {
        try {
            normalizeDocument(doc);

            return doc.getElementsByTagName("security");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
