package xml;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.Security;
import org.junit.Test;
import org.schalm.test.AbstractTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import services.SecurityService;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static constants.PathConstants.SAVE_FILE;
import static org.junit.Assert.*;

public class XmlFileWriterTest extends AbstractTest {
    XmlHelper xmlHelper = new XmlHelper();
    SecurityService service = new SecurityService();
    XmlFileWriter xmlFileWriter = new XmlFileWriter();

    private List<Security> loadSecurities() throws IOException, ParserConfigurationException, SAXException {
        Document document = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "EtfSecurity.xml").toPath()));
        NodeList securityNodes = document.getElementsByTagName("security");
        List<Security> securityList = service.processSecurities(securityNodes);

        return securityList;
    }

    @Test
    public void updateXml() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "Portfolio Performance Single.xml").toPath()));
        List<Security> securities = loadSecurities();

        xmlFileWriter.updateXml(portfolioDocument, securities);
    }

    @Test
    public void importBranches() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "Portfolio Performance Single.xml").toPath()));
        List<Security> securities = loadSecurities();
        //JsonObject cacheFileJson = JsonParser.parseReader(new FileReader(SAVE_FILE)).getAsJsonObject();
        //JsonArray cachedBranches = cacheFileJson.get("branches").getAsJsonArray();
        JsonArray cachedBranches = new JsonArray();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Branchen (GICS)")) {
                    JsonArray importedBranches = xmlFileWriter.importBranches(portfolioDocument, securities, cachedBranches, taxonomyElement);
                }
            }
        }
    }
}
