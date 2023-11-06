package xml;

import com.google.gson.JsonArray;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class XmlFileWriterTest extends AbstractTest {
    private static final Logger logger = Logger.getLogger(XmlFileWriterTest.class.getCanonicalName());
    XmlHelper xmlHelper = new XmlHelper();
    SecurityService service = new SecurityService();
    XmlFileWriter xmlFileWriter = new XmlFileWriter();

    private List<Security> loadTestSecurity() throws IOException, ParserConfigurationException, SAXException {
        Document document = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "EtfSecurity.xml").toPath()));
        NodeList securityNodes = document.getElementsByTagName("security");
        List<Security> securityList = service.processSecurities(securityNodes);
        logger.info("Loaded " + securityList.size() + " securities from file");

        return securityList;
    }

    @Test
    public void updateXml() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "Portfolio Performance Single.xml").toPath()));
        List<Security> securities = loadTestSecurity();

        xmlFileWriter.updateXml(portfolioDocument, securities);
    }

    @Test
    public void importBranches() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "Portfolio Performance Single.xml").toPath()));
        List<Security> securities = loadTestSecurity();
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
                    assertEquals(9, importedBranches.size());
                }
            }
        }
    }

    @Test
    public void importTopTen() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "Portfolio Performance.xml").toPath()));
        List<Security> securities = service.processSecurities(XmlFileReader.getAllSecurities(portfolioDocument));
        JsonArray cachedBranches = new JsonArray();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Top Ten")) {
                    JsonArray importedTopTen = xmlFileWriter.importTopTen(portfolioDocument, securities, cachedBranches, taxonomyElement);
                    assertEquals(190, importedTopTen.size());
                }
            }
        }
    }

    @Test
    public void testReduceDistinctStrings() throws IOException {
        List<String> input;
        try (Stream<String> lines = Files.lines(Paths.get(BASE_TEST_PATH + "StockNames-input.txt"))) {
            input = lines.collect(Collectors.toList());
            assertEquals(176, input.size());
        }
        TreeMap<String, List<String>> result = xmlFileWriter.reduceSimilarStrings(input);
        assertEquals(123, result.size());
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            if (!entry.getValue().isEmpty())
                logger.info(entry.getKey() + ": " + entry.getValue());
        }
    }

}
