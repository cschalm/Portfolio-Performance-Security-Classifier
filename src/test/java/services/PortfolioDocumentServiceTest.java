package services;

import com.google.gson.JsonArray;
import models.Security;
import models.SecurityDetailsCache;
import org.junit.Test;
import org.schalm.test.AbstractTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import xml.XmlFileReader;
import xml.XmlHelper;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static constants.PathConstants.BASE_TARGET_PATH;
import static org.junit.Assert.*;

public class PortfolioDocumentServiceTest extends AbstractTest {
    private static final Logger logger = Logger.getLogger(PortfolioDocumentServiceTest.class.getCanonicalName());
    XmlHelper xmlHelper = new XmlHelper();
    SecurityService securityService = new SecurityService();
    PortfolioDocumentService portfolioDocumentService = new PortfolioDocumentService();

    private List<Security> loadTestSecurity() throws IOException, ParserConfigurationException, SAXException {
        Document document = xmlHelper.readXmlStream(BASE_TEST_PATH + "EtfSecurity.xml");
        NodeList securityNodes = document.getElementsByTagName("security");
        List<Security> securityList = securityService.processSecurities(securityNodes);
        logger.info("Loaded " + securityList.size() + " securities from file");

        return securityList;
    }

    @Test
    public void updateXml_IE00BYYHSM20() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        List<Security> securities = loadTestSecurity();

        SecurityDetailsCache securityDetailsCache = new SecurityDetailsCache(BASE_TARGET_PATH + "test-classes/IE00BYYHSM20-" + UUID.randomUUID() + ".json");

        portfolioDocumentService.updateXml(portfolioDocument, securities, securityDetailsCache);
        assertEquals("Countries", 14, securityDetailsCache.getCachedCountries().asList().size());
        assertEquals("Branches", 9, securityDetailsCache.getCachedBranches().asList().size());
        assertEquals("Top 10", 10, securityDetailsCache.getCachedTopTen().asList().size());
    }

    @Test
    public void updateXml_IE000CNSFAR2() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        SecurityService service = new SecurityService();
        Security security = service.createSecurity("IE000CNSFAR2");
        assertNotNull(security);
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        SecurityDetailsCache securityDetailsCache = new SecurityDetailsCache(BASE_TARGET_PATH + "test-classes/IE000CNSFAR2-" + UUID.randomUUID() + ".json");

        portfolioDocumentService.updateXml(portfolioDocument, securities, securityDetailsCache);
        assertEquals("Countries", 32, securityDetailsCache.getCachedCountries().asList().size());
        assertEquals("Branches", 11, securityDetailsCache.getCachedBranches().asList().size());
        assertEquals("Top 10", 9, securityDetailsCache.getCachedTopTen().asList().size());
    }

    @Test
    public void importBranches_IE00BYYHSM20() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        List<Security> securities = loadTestSecurity();
        JsonArray cachedBranches = new JsonArray();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Branchen (GICS)")) {
                    JsonArray importedBranches = portfolioDocumentService.importBranches(portfolioDocument, securities, cachedBranches, taxonomyElement);
                    assertEquals(9, importedBranches.size());
                }
            }
        }
    }

    @Test
    public void importBranches_IE000CNSFAR2() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
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
                    JsonArray importedBranches = portfolioDocumentService.importBranches(portfolioDocument, securities, cachedBranches, taxonomyElement);
                    assertEquals(11, importedBranches.size());
                }
            }
        }
    }

    @Test
    public void importTopTen() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        List<Security> securities = securityService.processSecurities(Objects.requireNonNull(XmlFileReader.getAllSecurities(portfolioDocument)));
        JsonArray cachedBranches = new JsonArray();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Top Ten")) {
                    JsonArray importedTopTen = portfolioDocumentService.importTopTen(portfolioDocument, securities, cachedBranches, taxonomyElement);
                    assertEquals(10, importedTopTen.size());
                }
            }
        }
    }

    @Test
    public void importTopTen_IE000CNSFAR2() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
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
                    JsonArray importedTopTen = portfolioDocumentService.importTopTen(portfolioDocument, securities, cachedTopTen, taxonomyElement);
                    assertEquals(9, importedTopTen.size());
                }
            }
        }
    }

    @Test
    public void importTopTen_IE00BYYHSM20() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
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
                    JsonArray importedTopTen = portfolioDocumentService.importTopTen(portfolioDocument, securities, cachedTopTen, taxonomyElement);
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
        TreeMap<String, List<String>> result = portfolioDocumentService.reduceSimilarStrings(input);
        assertEquals(129, result.size());
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            if (!entry.getValue().isEmpty()) logger.info(entry.getKey() + ": " + entry.getValue());
        }
    }

    @Test
    public void importCountries_IE000CNSFAR2() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2");
        assertNotNull(security);
        assertNotNull(security.getCountries());
        assertEquals(32, security.getCountries().size());
        logger.info("Countries from Security: " + security.getCountries().keySet().stream().sorted().collect(Collectors.toList()));
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
                    JsonArray importedCountries = portfolioDocumentService.importRegions(portfolioDocument, securities, cachedCountries, taxonomyElement);
                    assertEquals(32, importedCountries.size());
                }
            }
        }
    }

    @Test
    public void collectAllStockNames() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
//        Document portfolioDocument = xmlHelper.readXmlStream(BASE_PATH + INPUT_FILE_NAME);
        List<Security> securities = securityService.processSecurities(Objects.requireNonNull(XmlFileReader.getAllSecurities(portfolioDocument)));
        TreeMap<String, List<String>> allStockNames = portfolioDocumentService.collectAllStockNames(securities);
        assertNotNull(allStockNames);
        assertEquals(10, allStockNames.size());
    }

    @Test
    public void testReduceDistinctStrings2() throws IOException {
        List<String> input;
        try (Stream<String> lines = Files.lines(Paths.get(BASE_TEST_PATH + "StockNames-input2.txt"))) {
            input = lines.collect(Collectors.toList());
            assertEquals(168, input.size());
        }
        TreeMap<String, List<String>> result = portfolioDocumentService.reduceSimilarStrings(input);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            sb.append(entry.getKey()).append('\n');
        }
        try (PrintWriter savingImport = new PrintWriter(BASE_TARGET_PATH + "StockNames-distinct3.txt", StandardCharsets.UTF_8)) {
            savingImport.print(sb);
        } catch (IOException e) {
            logger.warning("List of all StockNames could not be saved: " + e.getMessage());
        }
        String expected;
        try (Stream<String> lines = Files.lines(Paths.get(BASE_TEST_PATH + "StockNames-distinct2.txt"))) {
            expected = lines.collect(Collectors.joining("\n"));
            assertEquals(expected.trim(), sb.toString().trim());
        }
        assertEquals(134, result.size());
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            if (!entry.getValue().isEmpty()) logger.info(entry.getKey() + ": " + entry.getValue());
        }
    }

    @Test
    public void isNameSimilar() {
        assertTrue(portfolioDocumentService.isNameSimilar("SAP", "SAP SE"));
        assertTrue(portfolioDocumentService.isNameSimilar("SAP", "Sap SE"));
        assertTrue(portfolioDocumentService.isNameSimilar("Sap", "SAP SE"));
        assertFalse(portfolioDocumentService.isNameSimilar("AXA", "ABB"));
        assertFalse(portfolioDocumentService.isNameSimilar("Novartis AG", "Novo Nordisk"));
        assertTrue(portfolioDocumentService.isNameSimilar("ALPHABET INC CL A", "ALPHABET INC CL C"));
        assertTrue(portfolioDocumentService.isNameSimilar("Alphabet A (Google)", "Alphabet C (Google)"));
        assertTrue(portfolioDocumentService.isNameSimilar("Alphabet A (Google)", "Alphabet Inc."));
        assertTrue(portfolioDocumentService.isNameSimilar("Alphabet Inc.", "Alphabet Inc."));
        assertTrue(portfolioDocumentService.isNameSimilar("Meta Platforms (ehem. Facebook)", "Meta Platforms Inc."));
        assertTrue(portfolioDocumentService.isNameSimilar("SAMSUNG ELECTRONIC CO LTD", "Samsung Electronics Co. Ltd."));
        assertFalse(portfolioDocumentService.isNameSimilar("Mitsui & Co. Ltd.", "Mitsui O.S.K. Lines"));
    }

    @Test
    public void testOptimizeBranchNameFromSecurity() {
        assertEquals("Banken", portfolioDocumentService.optimizeBranchNameFromSecurity("Banken"));
        assertEquals("Basiskonsumgüter", portfolioDocumentService.optimizeBranchNameFromSecurity("Basiskonsumgüter"));
        assertEquals("Baumaterialien", portfolioDocumentService.optimizeBranchNameFromSecurity("Baumaterialien/Baukomponenten"));
        assertEquals("Chemikalien", portfolioDocumentService.optimizeBranchNameFromSecurity("Chemikalien"));
        assertEquals("Hardware Technologie, Speicherung & Peripherie", portfolioDocumentService.optimizeBranchNameFromSecurity("Computerherstellung"));
        assertEquals("Verschiedene REITs", portfolioDocumentService.optimizeBranchNameFromSecurity("Diversifizierte REITs"));
        assertEquals("Drahtlose Telekommunikationsdienste", portfolioDocumentService.optimizeBranchNameFromSecurity("Drahtlose Telekommunikationsdienste"));
        assertEquals("Handels-REITs", portfolioDocumentService.optimizeBranchNameFromSecurity("Einzelhandel REITs"));
        assertEquals("Elektronische Komponenten", portfolioDocumentService.optimizeBranchNameFromSecurity("Elektrokomponenten"));
        assertEquals("Elektronische Geräte & Instrumente", portfolioDocumentService.optimizeBranchNameFromSecurity("Elektrokomponenten & -geräte"));
        assertEquals("Energie", portfolioDocumentService.optimizeBranchNameFromSecurity("Energie"));
        assertEquals("Automobilbranche", portfolioDocumentService.optimizeBranchNameFromSecurity("Fahrzeugbau"));
        assertEquals("Private Finanzdienste", portfolioDocumentService.optimizeBranchNameFromSecurity("Finanzdienstleistungen"));
        assertEquals("Finanzwesen", portfolioDocumentService.optimizeBranchNameFromSecurity("Finanzen"));
        assertEquals("Gesundheitswesen", portfolioDocumentService.optimizeBranchNameFromSecurity("Gesundheitswesen"));
        assertEquals("Halbleiter", portfolioDocumentService.optimizeBranchNameFromSecurity("Halbleiterelektronik"));
        assertEquals("Hardware Technologie, Speicherung & Peripherie", portfolioDocumentService.optimizeBranchNameFromSecurity("Hardware- Technologie, Speicherung und Peripheriegeräte"));
        assertEquals("Hotels, Restaurants und Freizeit", portfolioDocumentService.optimizeBranchNameFromSecurity("Hotels, Restaurants und Freizeit"));
        assertEquals("Hypotheken-, Immobilien-, Investment-, Trusts (REITs)", portfolioDocumentService.optimizeBranchNameFromSecurity("Hypotheken-Immobilien-fonds (REITs)"));
        assertEquals("Informationstechnologie", portfolioDocumentService.optimizeBranchNameFromSecurity("IT/Telekommunikation"));
        assertEquals("Immobilien", portfolioDocumentService.optimizeBranchNameFromSecurity("Immobilien"));
        assertEquals("Industrie", portfolioDocumentService.optimizeBranchNameFromSecurity("Industrie"));
        assertEquals("Industriemaschinen", portfolioDocumentService.optimizeBranchNameFromSecurity("Industriemaschinenbau"));
        assertEquals("Informationstechnologie", portfolioDocumentService.optimizeBranchNameFromSecurity("Informationstechnologie"));
        assertEquals("Basiskonsumgüter", portfolioDocumentService.optimizeBranchNameFromSecurity("Konsumgüter"));
        assertEquals("Nicht-Basiskonsumgüter", portfolioDocumentService.optimizeBranchNameFromSecurity("Konsumgüter zyklisch"));
        assertEquals("Roh-, Hilfs- & Betriebsstoffe", portfolioDocumentService.optimizeBranchNameFromSecurity("Rohstoffe"));
        assertEquals("Rückversicherung", portfolioDocumentService.optimizeBranchNameFromSecurity("Rückversicherung"));
        assertEquals("Software", portfolioDocumentService.optimizeBranchNameFromSecurity("Software"));
        assertEquals("Telekommunikationsdienste", portfolioDocumentService.optimizeBranchNameFromSecurity("Telekomdienste"));
        assertEquals("Telekommunikationsdienste", portfolioDocumentService.optimizeBranchNameFromSecurity("Telekommunikation"));
        assertEquals("Verbraucherelektronik", portfolioDocumentService.optimizeBranchNameFromSecurity("Verbraucherelektronik"));
        assertEquals("Versicherung", portfolioDocumentService.optimizeBranchNameFromSecurity("Versicherung"));
        assertEquals("Versorgungsbetriebe", portfolioDocumentService.optimizeBranchNameFromSecurity("Versorger"));
        assertEquals("", portfolioDocumentService.optimizeBranchNameFromSecurity("diverse Branchen"));
    }

    @Test
    public void testGetBestMatch() {
        Collection<String> possibleBranches = Set.of("Handels-REITs", "Verschiedene REITs", "Industrielle REITs", "Hotel und Resort REITs",
                "Büro-REITs", "Gesundheitswesen REITs", "Privater Wohnungsbau-REITs", "Spezialisierte REITs",
                "Hypotheken-, Immobilien-, Investment-, Trusts (REITs)", "Hypotheken-REITs");
        PortfolioDocumentService.BestMatch bestMatch = portfolioDocumentService.getBestMatch(possibleBranches, "Einzelhandel REITs");
        assertEquals("Handels-REITs", bestMatch.bestMatchingBranchName);
        bestMatch = portfolioDocumentService.getBestMatch(possibleBranches, "Hypotheken-Immobilien-fonds (REITs)");
        assertEquals("Hypotheken-REITs", bestMatch.bestMatchingBranchName);

        possibleBranches = Set.of("Versorgungsbetriebe", "Stromversorgungsbetriebe", "Multi-Versorger", "Gasversorgungsbetriebe",
                "Wasserversorgungsbetriebe", "Unabhängige Energie- und Erneuerbare Elektrizitätshersteller",
                "Unabhängige Energiehersteller und -händler", "Erneuerbare Elektrizität");
        bestMatch = portfolioDocumentService.getBestMatch(possibleBranches, "Versorger");
        assertEquals("Multi-Versorger", bestMatch.bestMatchingBranchName);
    }

}
