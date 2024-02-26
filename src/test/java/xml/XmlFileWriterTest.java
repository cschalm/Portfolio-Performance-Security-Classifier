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
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static constants.PathConstants.BASE_TARGET_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    public void updateXml_IE00BYYHSM20() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "Portfolio Performance Single.xml").toPath()));
        List<Security> securities = loadTestSecurity();

        SecurityDetailsCache securityDetailsCache = new SecurityDetailsCache(BASE_TARGET_PATH + "test-classes/IE00BYYHSM20-" + UUID.randomUUID().toString() + ".json");

        xmlFileWriter.updateXml(portfolioDocument, securities, securityDetailsCache);
        assertEquals("Countries", 14, securityDetailsCache.getCachedCountries().asList().size());
        assertEquals("Branches", 9, securityDetailsCache.getCachedBranches().asList().size());
        assertEquals("Top 10", 10, securityDetailsCache.getCachedTopTen().asList().size());
    }

    @Test
    public void updateXml_IE000CNSFAR2() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "Portfolio Performance Single.xml").toPath()));
        SecurityService service = new SecurityService();
        Security security = service.createSecurity("IE000CNSFAR2");
        assertNotNull(security);
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        SecurityDetailsCache securityDetailsCache = new SecurityDetailsCache(BASE_TARGET_PATH + "test-classes/IE000CNSFAR2-" + UUID.randomUUID().toString() + ".json");

        xmlFileWriter.updateXml(portfolioDocument, securities, securityDetailsCache);
        assertEquals("Countries", 33, securityDetailsCache.getCachedCountries().asList().size());
        assertEquals("Branches", 11, securityDetailsCache.getCachedBranches().asList().size());
        assertEquals("Top 10", 9, securityDetailsCache.getCachedTopTen().asList().size());
    }

    @Test
    public void importBranches_IE00BYYHSM20() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "Portfolio Performance Single.xml").toPath()));
        List<Security> securities = loadTestSecurity();
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
    public void importBranches_IE000CNSFAR2() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "Portfolio Performance Single.xml").toPath()));
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2");
        assertNotNull(security);
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);
        JsonArray cachedBranches = new JsonArray();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Branchen (GICS)")) {
                    JsonArray importedBranches = xmlFileWriter.importBranches(portfolioDocument, securities, cachedBranches, taxonomyElement);
                    assertEquals(11, importedBranches.size());
                }
            }
        }
    }

    @Test
    public void importTopTen() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "Portfolio Performance Single.xml").toPath()));
        List<Security> securities = service.processSecurities(Objects.requireNonNull(XmlFileReader.getAllSecurities(portfolioDocument)));
        JsonArray cachedBranches = new JsonArray();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Top Ten")) {
                    JsonArray importedTopTen = xmlFileWriter.importTopTen(portfolioDocument, securities, cachedBranches, taxonomyElement);
                    assertEquals(10, importedTopTen.size());
                }
            }
        }
    }

    @Test
    public void importTopTen_IE000CNSFAR2() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "Portfolio Performance Single.xml").toPath()));
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2");
        assertNotNull(security);
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);
        JsonArray cachedTopTen = new JsonArray();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Top Ten")) {
                    JsonArray importedTopTen = xmlFileWriter.importTopTen(portfolioDocument, securities, cachedTopTen, taxonomyElement);
                    assertEquals(9, importedTopTen.size());
                }
            }
        }
    }

    @Test
    public void importTopTen_IE00BYYHSM20() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "Portfolio Performance Single.xml").toPath()));
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE00BYYHSM20");
        assertNotNull(security);
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);
        JsonArray cachedTopTen = new JsonArray();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Top Ten")) {
                    JsonArray importedTopTen = xmlFileWriter.importTopTen(portfolioDocument, securities, cachedTopTen, taxonomyElement);
                    assertEquals(10, importedTopTen.size());
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

    @Test
    public void importCountries_IE000CNSFAR2() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "Portfolio Performance Single.xml").toPath()));
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2");
        assertNotNull(security);
        assertNotNull(security.getCountries());
        assertEquals(34, security.getCountries().size());
        logger.info("Countries from Security: " + security.getCountries().keySet());
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);
        JsonArray cachedCountries = new JsonArray();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Regionen")) {
                    JsonArray importedCountries = xmlFileWriter.importRegions(portfolioDocument, securities, cachedCountries, taxonomyElement);
                    assertEquals(33, importedCountries.size());
                }
            }
        }
    }

}
