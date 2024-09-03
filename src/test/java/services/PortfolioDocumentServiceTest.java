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
        assertEquals("Branches", 9, securityDetailsCache.getCachedIndustries().asList().size());
        assertEquals("Top 10", 10, securityDetailsCache.getCachedTopTen().asList().size());
    }

    @Test
    public void updateXml_IE000CNSFAR2() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        SecurityService service = new SecurityService();
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        SecurityDetailsCache securityDetailsCache = new SecurityDetailsCache(BASE_TARGET_PATH + "test-classes/IE000CNSFAR2-" + UUID.randomUUID() + ".json");

        portfolioDocumentService.updateXml(portfolioDocument, securities, securityDetailsCache);
        assertEquals("Countries", 24, securityDetailsCache.getCachedCountries().asList().size());
        assertEquals("Branches", 11, securityDetailsCache.getCachedIndustries().asList().size());
        assertEquals("Top 10", 10, securityDetailsCache.getCachedTopTen().asList().size());
    }

    @Test
    public void importBranches_IE00BYYHSM20() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        List<Security> securities = loadTestSecurity();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Branchen (GICS)")) {
                    JsonArray importedBranches = portfolioDocumentService.importIndustries(portfolioDocument, securities, taxonomyElement);
                    assertEquals(9, importedBranches.size());
                }
            }
        }
    }

    @Test
    public void importBranches_IE000CNSFAR2() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Branchen (GICS)")) {
                    JsonArray importedBranches = portfolioDocumentService.importIndustries(portfolioDocument, securities, taxonomyElement);
                    assertEquals(11, importedBranches.size());
                }
            }
        }
    }

    @Test
    public void importTopTen() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        List<Security> securities = securityService.processSecurities(Objects.requireNonNull(new XmlFileReader().getAllSecurities(portfolioDocument)));

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Unternehmensgewichtung")) {
                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, taxonomyElement);
                    assertEquals(10, importedTopTen.size());
                }
            }
        }
    }

    @Test
    public void importTopTen_IE000CNSFAR2() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Unternehmensgewichtung")) {
                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, taxonomyElement);
                    assertEquals(10, importedTopTen.size());
                }
            }
        }
    }

    @Test
    public void importTopTen_IE00BYYHSM20() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE00BYYHSM20", 0);
        assertNotNull(security);
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Unternehmensgewichtung")) {
                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, taxonomyElement);
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
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        assertNotNull(security.getCountries());
        assertEquals(32, security.getCountries().size());
        logger.info("Countries from Security: " + security.getCountries().keySet().stream().sorted().collect(Collectors.toList()));
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Regionen")) {
                    JsonArray importedCountries = portfolioDocumentService.importRegions(portfolioDocument, securities, taxonomyElement);
                    assertEquals(32, importedCountries.size());
                }
            }
        }
    }

    @Test
    public void collectAllStockNames() throws IOException, ParserConfigurationException, SAXException {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
//        Document portfolioDocument = xmlHelper.readXmlStream(BASE_PATH + INPUT_FILE_NAME);
        List<Security> securities = securityService.processSecurities(Objects.requireNonNull(new XmlFileReader().getAllSecurities(portfolioDocument)));
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
        assertTrue(portfolioDocumentService.isNameSimilar("ALPHABET INC CL A", "ALPHABET INC CL C"));
        assertTrue(portfolioDocumentService.isNameSimilar("Alphabet A (Google)", "Alphabet C (Google)"));
        assertTrue(portfolioDocumentService.isNameSimilar("Alphabet A (Google)", "Alphabet Inc."));
        assertTrue(portfolioDocumentService.isNameSimilar("Alphabet Inc.", "Alphabet Inc."));
        assertTrue(portfolioDocumentService.isNameSimilar("Meta Platforms (ehem. Facebook)", "Meta Platforms Inc."));
        assertTrue(portfolioDocumentService.isNameSimilar("SAMSUNG ELECTRONIC CO LTD", "Samsung Electronics Co. Ltd."));
        assertTrue(portfolioDocumentService.isNameSimilar("Nvidia", "NVIDIA Corp."));
        assertTrue(portfolioDocumentService.isNameSimilar("ING Groep", "ING Group"));
        assertFalse(portfolioDocumentService.isNameSimilar("Mitsui & Co. Ltd.", "Mitsui O.S.K. Lines"));
        assertFalse(portfolioDocumentService.isNameSimilar("AXA", "ABB"));
        assertFalse(portfolioDocumentService.isNameSimilar("Novartis AG", "Novo Nordisk"));
        assertFalse(portfolioDocumentService.isNameSimilar("Deutsche Bank", "Deutsche Telekom"));
        assertFalse(portfolioDocumentService.isNameSimilar("Deutsche Bank", "Deutsche Post"));
        assertFalse(portfolioDocumentService.isNameSimilar("Deutsche Post", "Deutsche Telekom"));
    }

    @Test
    public void testOptimizeBranchNameFromSecurity() {
        assertEquals("Banken", portfolioDocumentService.optimizeIndustryNameFromSecurity("Banken"));
        assertEquals("Basiskonsumgüter", portfolioDocumentService.optimizeIndustryNameFromSecurity("Basiskonsumgüter"));
        assertEquals("Baumaterialien", portfolioDocumentService.optimizeIndustryNameFromSecurity("Baumaterialien/Baukomponenten"));
        assertEquals("Chemikalien", portfolioDocumentService.optimizeIndustryNameFromSecurity("Chemikalien"));
        assertEquals("Hardware Technologie, Speicherung & Peripherie", portfolioDocumentService.optimizeIndustryNameFromSecurity("Computerherstellung"));
        assertEquals("Verschiedene REITs", portfolioDocumentService.optimizeIndustryNameFromSecurity("Diversifizierte REITs"));
        assertEquals("Drahtlose Telekommunikationsdienste", portfolioDocumentService.optimizeIndustryNameFromSecurity("Drahtlose Telekommunikationsdienste"));
        assertEquals("Handels-REITs", portfolioDocumentService.optimizeIndustryNameFromSecurity("Einzelhandel REITs"));
        assertEquals("Elektronische Komponenten", portfolioDocumentService.optimizeIndustryNameFromSecurity("Elektrokomponenten"));
        assertEquals("Elektronische Geräte & Instrumente", portfolioDocumentService.optimizeIndustryNameFromSecurity("Elektrokomponenten & -geräte"));
        assertEquals("Energie", portfolioDocumentService.optimizeIndustryNameFromSecurity("Energie"));
        assertEquals("Automobilbranche", portfolioDocumentService.optimizeIndustryNameFromSecurity("Fahrzeugbau"));
        assertEquals("Automobilbranche", portfolioDocumentService.optimizeIndustryNameFromSecurity("Kraftfahrzeuge"));
        assertEquals("Private Finanzdienste", portfolioDocumentService.optimizeIndustryNameFromSecurity("Finanzdienstleistungen"));
        assertEquals("Finanzwesen", portfolioDocumentService.optimizeIndustryNameFromSecurity("Finanzen"));
        assertEquals("Gesundheitswesen", portfolioDocumentService.optimizeIndustryNameFromSecurity("Gesundheitswesen"));
        assertEquals("Halbleiter", portfolioDocumentService.optimizeIndustryNameFromSecurity("Halbleiterelektronik"));
        assertEquals("Hardware Technologie, Speicherung & Peripherie", portfolioDocumentService.optimizeIndustryNameFromSecurity("Hardware- Technologie, Speicherung und Peripheriegeräte"));
        assertEquals("Hotels, Restaurants und Freizeit", portfolioDocumentService.optimizeIndustryNameFromSecurity("Hotels, Restaurants und Freizeit"));
        assertEquals("Hypotheken-, Immobilien-, Investment-, Trusts (REITs)", portfolioDocumentService.optimizeIndustryNameFromSecurity("Hypotheken-Immobilien-fonds (REITs)"));
        assertEquals("Informationstechnologie", portfolioDocumentService.optimizeIndustryNameFromSecurity("IT/Telekommunikation"));
        assertEquals("Immobilien", portfolioDocumentService.optimizeIndustryNameFromSecurity("Immobilien"));
        assertEquals("Industrie", portfolioDocumentService.optimizeIndustryNameFromSecurity("Industrie"));
        assertEquals("Industriemaschinen", portfolioDocumentService.optimizeIndustryNameFromSecurity("Industriemaschinenbau"));
        assertEquals("Informationstechnologie", portfolioDocumentService.optimizeIndustryNameFromSecurity("Informationstechnologie"));
        assertEquals("Basiskonsumgüter", portfolioDocumentService.optimizeIndustryNameFromSecurity("Konsumgüter"));
        assertEquals("Nicht-Basiskonsumgüter", portfolioDocumentService.optimizeIndustryNameFromSecurity("Konsumgüter zyklisch"));
        assertEquals("Roh-, Hilfs- & Betriebsstoffe", portfolioDocumentService.optimizeIndustryNameFromSecurity("Rohstoffe"));
        assertEquals("Rückversicherung", portfolioDocumentService.optimizeIndustryNameFromSecurity("Rückversicherung"));
        assertEquals("Software", portfolioDocumentService.optimizeIndustryNameFromSecurity("Software"));
        assertEquals("Telekommunikationsdienste", portfolioDocumentService.optimizeIndustryNameFromSecurity("Telekomdienste"));
        assertEquals("Telekommunikationsdienste", portfolioDocumentService.optimizeIndustryNameFromSecurity("Telekommunikation"));
        assertEquals("Verbraucherelektronik", portfolioDocumentService.optimizeIndustryNameFromSecurity("Verbraucherelektronik"));
        assertEquals("Versicherung", portfolioDocumentService.optimizeIndustryNameFromSecurity("Versicherung"));
        assertEquals("Versorgungsbetriebe", portfolioDocumentService.optimizeIndustryNameFromSecurity("Versorger"));
        assertEquals("", portfolioDocumentService.optimizeIndustryNameFromSecurity("diverse Branchen"));
    }

    @Test
    public void testGetBestMatch() {
        Collection<String> possibleBranches = Set.of("Handels-REITs", "Verschiedene REITs", "Industrielle REITs", "Hotel und Resort REITs",
                "Büro-REITs", "Gesundheitswesen REITs", "Privater Wohnungsbau-REITs", "Spezialisierte REITs",
                "Hypotheken-, Immobilien-, Investment-, Trusts (REITs)", "Hypotheken-REITs");
        PortfolioDocumentService.BestMatch bestMatch = portfolioDocumentService.getBestMatch(possibleBranches, "Einzelhandel REITs");
        assertEquals("Handels-REITs", bestMatch.bestMatchingIndustryName);
        bestMatch = portfolioDocumentService.getBestMatch(possibleBranches, "Hypotheken-Immobilien-fonds (REITs)");
        assertEquals("Hypotheken-REITs", bestMatch.bestMatchingIndustryName);

        possibleBranches = Set.of("Versorgungsbetriebe", "Stromversorgungsbetriebe", "Multi-Versorger", "Gasversorgungsbetriebe",
                "Wasserversorgungsbetriebe", "Unabhängige Energie- und Erneuerbare Elektrizitätshersteller",
                "Unabhängige Energiehersteller und -händler", "Erneuerbare Elektrizität");
        bestMatch = portfolioDocumentService.getBestMatch(possibleBranches, "Versorger");
        assertEquals("Multi-Versorger", bestMatch.bestMatchingIndustryName);
    }

    @Test
    public void testFindAssignmentBySecurityIndex() throws IOException, ParserConfigurationException, SAXException {
        Document document = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-country.xml");
        Node classification = document.getFirstChild();
        Element assessment = portfolioDocumentService.findAssignmentBySecurityIndex(classification, 4);
        assertNotNull(assessment);
        String weight = xmlHelper.getTextContent(assessment, "weight");
        assertNotNull(weight);
        assertEquals("804", weight);
        String rank = xmlHelper.getTextContent(assessment, "rank");
        assertNotNull(rank);
        assertEquals("0", rank);

        assessment = portfolioDocumentService.findAssignmentBySecurityIndex(classification, 49);
        assertNotNull(assessment);
        weight = xmlHelper.getTextContent(assessment, "weight");
        assertNotNull(weight);
        assertEquals("510", weight);
        rank = xmlHelper.getTextContent(assessment, "rank");
        assertNotNull(rank);
        assertEquals("4", rank);
    }

    @Test
    public void testUpdateWeightOfAssignmentCountry() throws IOException, ParserConfigurationException, SAXException {
        Document document = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-country.xml");
        Node classification = document.getFirstChild();
        Element assessment = portfolioDocumentService.findAssignmentBySecurityIndex(classification, 4);
        assertNotNull(assessment);
        String weight = xmlHelper.getTextContent(assessment, "weight");
        assertNotNull(weight);
        assertEquals("804", weight);

        assessment = portfolioDocumentService.updateWeightOfAssignment(assessment, "408");
        assertNotNull(assessment);
        weight = xmlHelper.getTextContent(assessment, "weight");
        assertNotNull(weight);
        assertEquals("408", weight);
    }

    @Test
    public void testImportRegions_IE000CNSFAR2_Remove() throws IOException, ParserConfigurationException, SAXException {
        // "Tschechien" to be removed by import
        // "Ungarn" to be removed by import
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-country-IE000CNSFAR2.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        assertNotNull(security.getCountries());
        assertEquals(32, security.getCountries().size());
        logger.info("Countries from Security: " + security.getCountries().keySet().stream().sorted().collect(Collectors.toList()));
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Regionen")) {
                    Element tschechien = portfolioDocumentService.findClassificationByName(taxonomyElement, "Tschechien");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(tschechien, 1);
                    assertNotNull(assignment);
                    Element ungarn = portfolioDocumentService.findClassificationByName(taxonomyElement, "Ungarn");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(ungarn, 1);
                    assertNotNull(assignment);
                    Element finnland = portfolioDocumentService.findClassificationByName(taxonomyElement, "Finnland");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(finnland, 1);
                    assertNotNull(assignment);

                    JsonArray importedCountries = portfolioDocumentService.importRegions(portfolioDocument, securities, taxonomyElement);
                    assertEquals(2, importedCountries.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(tschechien, 1);
                    assertNull(assignment);
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(ungarn, 1);
                    assertNull(assignment);
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(finnland, 1);
                    assertNotNull(assignment);
                }
            }
        }
    }

    @Test
    public void testImportRegions_IE000CNSFAR2_Add() throws IOException, ParserConfigurationException, SAXException {
        // "Italien" to add
        // "Portugal" to add
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-country-IE000CNSFAR2.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        assertNotNull(security.getCountries());
        assertEquals(32, security.getCountries().size());
        logger.info("Countries from Security: " + security.getCountries().keySet().stream().sorted().collect(Collectors.toList()));
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Regionen")) {
                    Element grossbritannien = portfolioDocumentService.findClassificationByName(taxonomyElement, "Großbritannien");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(grossbritannien, 1);
                    assertNotNull(assignment);
                    assertEquals("278", getWeightOfAssignment(assignment));
                    Element italien = portfolioDocumentService.findClassificationByName(taxonomyElement, "Italien");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(italien, 1);
                    assertNull(assignment);
                    Element portugal = portfolioDocumentService.findClassificationByName(taxonomyElement, "Portugal");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(portugal, 1);
                    assertNull(assignment);

                    JsonArray importedCountries = portfolioDocumentService.importRegions(portfolioDocument, securities, taxonomyElement);
                    assertEquals(2, importedCountries.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(grossbritannien, 1);
                    assertNotNull(assignment);
                    assertEquals("278", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(italien, 1);
                    assertNotNull(assignment);
                    assertEquals("62", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(portugal, 1);
                    assertNotNull(assignment);
                    assertEquals("4", getWeightOfAssignment(assignment));
                }
            }
        }
    }

    @Test
    public void testImportRegions_IE000CNSFAR2_Update() throws IOException, ParserConfigurationException, SAXException {
        // "Dänemark" to update
        // "Finnland" to update
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-country-IE000CNSFAR2.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        assertNotNull(security.getCountries());
        assertEquals(32, security.getCountries().size());
        logger.info("Countries from Security: " + security.getCountries().keySet().stream().sorted().collect(Collectors.toList()));
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Regionen")) {
                    Element grossbritannien = portfolioDocumentService.findClassificationByName(taxonomyElement, "Großbritannien");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(grossbritannien, 1);
                    assertNotNull(assignment);
                    assertEquals("278", getWeightOfAssignment(assignment));
                    Element daenemark = portfolioDocumentService.findClassificationByName(taxonomyElement, "Dänemark");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(daenemark, 1);
                    assertNotNull(assignment);
                    assertEquals("5", getWeightOfAssignment(assignment));
                    Element finnland = portfolioDocumentService.findClassificationByName(taxonomyElement, "Finnland");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(finnland, 1);
                    assertNotNull(assignment);
                    assertEquals("3", getWeightOfAssignment(assignment));

                    JsonArray importedCountries = portfolioDocumentService.importRegions(portfolioDocument, securities, taxonomyElement);
                    assertEquals(2, importedCountries.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(grossbritannien, 1);
                    assertNotNull(assignment);
                    assertEquals("278", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(daenemark, 1);
                    assertNotNull(assignment);
                    assertEquals("92", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(finnland, 1);
                    assertNotNull(assignment);
                    assertEquals("27", getWeightOfAssignment(assignment));
                }
            }
        }
    }

    private String getWeightOfAssignment(Element assignment) {
        return xmlHelper.getTextContent(assignment, "weight");
    }

    @Test
    public void testUpdateWeightOfAssignmentIndustry() throws IOException, ParserConfigurationException, SAXException {
        Document document = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-industry.xml");
        Node classification = document.getFirstChild();
        Element assessment = portfolioDocumentService.findAssignmentBySecurityIndex(classification, 38);
        assertNotNull(assessment);
        String weight = xmlHelper.getTextContent(assessment, "weight");
        assertNotNull(weight);
        assertEquals("37", weight);

        assessment = portfolioDocumentService.updateWeightOfAssignment(assessment, "408");
        assertNotNull(assessment);
        weight = xmlHelper.getTextContent(assessment, "weight");
        assertNotNull(weight);
        assertEquals("408", weight);
    }

    @Test
    public void testImportIndustries_IE000CNSFAR2_Remove() throws IOException, ParserConfigurationException, SAXException {
        // "Kapitalmärkte" to be removed by import
        // "Software" to be removed by import
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-industry-IE000CNSFAR2.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        assertNotNull(security.getIndustries());
        assertEquals(11, security.getIndustries().size());
        logger.info("Industries from Security: " + security.getIndustries().keySet().stream().sorted().collect(Collectors.toList()));
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Branchen (GICS)")) {
                    Element informationstechnologie = portfolioDocumentService.findClassificationByName(taxonomyElement, "Informationstechnologie");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(informationstechnologie, 1);
                    assertNotNull(assignment);
                    Element kapitalmaerkte = portfolioDocumentService.findClassificationByName(taxonomyElement, "Kapitalmärkte");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(kapitalmaerkte, 1);
                    assertNotNull(assignment);
                    Element software = portfolioDocumentService.findClassificationByName(taxonomyElement, "Software");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(software, 1);
                    assertNotNull(assignment);

                    JsonArray importedBranches = portfolioDocumentService.importIndustries(portfolioDocument, securities, taxonomyElement);
                    assertEquals(3, importedBranches.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(informationstechnologie, 1);
                    assertNotNull(assignment);
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(kapitalmaerkte, 1);
                    assertNull(assignment);
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(software, 1);
                    assertNull(assignment);
                }
            }
        }
    }

    @Test
    public void testImportIndustries_IE000CNSFAR2_Add() throws IOException, ParserConfigurationException, SAXException {
        // "Nicht-Basiskonsumgüter" to add 1092
        // "Basiskonsumgüter" to add 655
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-industry-IE000CNSFAR2.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        assertNotNull(security.getIndustries());
        assertEquals(11, security.getIndustries().size());
        logger.info("Industries from Security: " + security.getIndustries().keySet().stream().sorted().collect(Collectors.toList()));
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Branchen (GICS)")) {
                    Element informationstechnologie = portfolioDocumentService.findClassificationByName(taxonomyElement, "Informationstechnologie");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(informationstechnologie, 1);
                    assertNotNull(assignment);
                    assertEquals("2411", getWeightOfAssignment(assignment));
                    Element nichtBasisKonsumgueter = portfolioDocumentService.findClassificationByName(taxonomyElement, "Nicht-Basiskonsumgüter");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(nichtBasisKonsumgueter, 1);
                    assertNull(assignment);
                    Element basiskonsumgueter = portfolioDocumentService.findClassificationByName(taxonomyElement, "Basiskonsumgüter");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(basiskonsumgueter, 1);
                    assertNull(assignment);

                    JsonArray importedBranches = portfolioDocumentService.importIndustries(portfolioDocument, securities, taxonomyElement);
                    assertEquals(3, importedBranches.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(informationstechnologie, 1);
                    assertNotNull(assignment);
                    assertEquals("2411", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(nichtBasisKonsumgueter, 1);
                    assertNotNull(assignment);
                    assertEquals("1092", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(basiskonsumgueter, 1);
                    assertNotNull(assignment);
                    assertEquals("655", getWeightOfAssignment(assignment));
                }
            }
        }
    }

    @Test
    public void testImportIndustries_IE000CNSFAR2_Update() throws IOException, ParserConfigurationException, SAXException {
        // "Gesundheitswesen" to update 1205
        // "Industrie" to update 1113
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-industry-IE000CNSFAR2.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        assertNotNull(security.getIndustries());
        assertEquals(11, security.getIndustries().size());
        logger.info("Industries from Security: " + security.getIndustries().keySet().stream().sorted().collect(Collectors.toList()));
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Branchen (GICS)")) {
                    Element informationstechnologie = portfolioDocumentService.findClassificationByName(taxonomyElement, "Informationstechnologie");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(informationstechnologie, 1);
                    assertNotNull(assignment);
                    assertEquals("2411", getWeightOfAssignment(assignment));
                    Element gesundheitswesen = portfolioDocumentService.findClassificationByName(taxonomyElement, "Gesundheitswesen");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(gesundheitswesen, 1);
                    assertNotNull(assignment);
                    assertEquals("100", getWeightOfAssignment(assignment));
                    Element industrie = portfolioDocumentService.findClassificationByName(taxonomyElement, "Industrie");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(industrie, 1);
                    assertNotNull(assignment);
                    assertEquals("200", getWeightOfAssignment(assignment));

                    JsonArray importedBranches = portfolioDocumentService.importIndustries(portfolioDocument, securities, taxonomyElement);
                    assertEquals(3, importedBranches.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(informationstechnologie, 1);
                    assertNotNull(assignment);
                    assertEquals("2411", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(gesundheitswesen, 1);
                    assertNotNull(assignment);
                    assertEquals("1205", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(industrie, 1);
                    assertNotNull(assignment);
                    assertEquals("1113", getWeightOfAssignment(assignment));
                }
            }
        }
    }

    @Test
    public void testUpdateWeightOfAssignmentTopTen() throws IOException, ParserConfigurationException, SAXException {
        Document document = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten.xml");
        Node classification = document.getFirstChild();
        Element assessment = portfolioDocumentService.findAssignmentBySecurityIndex(classification, 20);
        assertNotNull(assessment);
        String weight = xmlHelper.getTextContent(assessment, "weight");
        assertNotNull(weight);
        assertEquals("213", weight);

        assessment = portfolioDocumentService.updateWeightOfAssignment(assessment, "312");
        assertNotNull(assessment);
        weight = xmlHelper.getTextContent(assessment, "weight");
        assertNotNull(weight);
        assertEquals("312", weight);
    }

    @Test
    public void testImportTopTen_IE000CNSFAR2_Remove() throws IOException, ParserConfigurationException, SAXException {
        // "ABB" to be removed by import
        // "Saia" to be removed by import
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-IE000CNSFAR2.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(10, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Unternehmensgewichtung")) {
                    Element abb = portfolioDocumentService.findClassificationByName(taxonomyElement, "ABB");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(abb, 1);
                    assertNotNull(assignment);
                    Element saia = portfolioDocumentService.findClassificationByName(taxonomyElement, "Saia");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(saia, 1);
                    assertNotNull(assignment);
                    Element nvidia = portfolioDocumentService.findClassificationByName(taxonomyElement, "NVIDIA Corp.");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(nvidia, 1);
                    assertNotNull(assignment);

                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, taxonomyElement);
                    assertEquals(3, importedTopTen.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(nvidia, 1);
                    assertNotNull(assignment);
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(saia, 1);
                    assertNull(assignment);
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(abb, 1);
                    assertNull(assignment);
                }
            }
        }
    }

    @Test
    public void testImportTopTen_IE000CNSFAR2_RemoveOnlyOneEntry() throws IOException, ParserConfigurationException, SAXException {
        // nothing to be removed by import
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-IE000CNSFAR2.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(10, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Unternehmensgewichtung")) {
                    Element tesla = portfolioDocumentService.findClassificationByName(taxonomyElement, "Tesla");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(tesla, 1);
                    assertNotNull(assignment);

                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, taxonomyElement);
                    assertEquals(3, importedTopTen.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(tesla, 1);
                    assertNotNull(assignment);
                }
            }
        }
    }

    @Test
    public void testImportTopTen_IE000CNSFAR2_RemoveOnlyOneEntryAndClassificationFolder() throws IOException, ParserConfigurationException, SAXException {
        // nothing to be removed by import
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-IE000CNSFAR2.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(10, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Unternehmensgewichtung")) {
                    Element amd = portfolioDocumentService.findClassificationByName(taxonomyElement, "AMD");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(amd, 1);
                    assertNotNull(assignment);

                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, taxonomyElement);
                    assertEquals(3, importedTopTen.size());

                    amd = portfolioDocumentService.findClassificationByName(taxonomyElement, "AMD");
                    assertNull(amd);
                }
            }
        }
    }

    @Test
    public void testImportTopTen_IE000CNSFAR2_Add() throws IOException, ParserConfigurationException, SAXException {
        // "Alphabet A (Google)" to add 130
        // "Eli Lilly & Co." to add 96
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-IE000CNSFAR2.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(10, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Unternehmensgewichtung")) {
                    Element amazon = portfolioDocumentService.findClassificationByName(taxonomyElement, "Amazon");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(amazon, 1);
                    assertNotNull(assignment);
                    assertEquals("260", getWeightOfAssignment(assignment));
                    Element alphabet = portfolioDocumentService.findClassificationByName(taxonomyElement, "Alphabet A (Google)");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(alphabet, 1);
                    assertNull(assignment);
                    Element eliLilly = portfolioDocumentService.findClassificationByName(taxonomyElement, "Eli Lilly & Co.");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(eliLilly, 1);
                    assertNull(assignment);

                    JsonArray importedBranches = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, taxonomyElement);
                    assertEquals(3, importedBranches.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(amazon, 1);
                    assertNotNull(assignment);
                    assertEquals("260", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(alphabet, 1);
                    assertNotNull(assignment);
                    assertEquals("130", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(eliLilly, 1);
                    assertNotNull(assignment);
                    assertEquals("96", getWeightOfAssignment(assignment));
                }
            }
        }
    }

    @Test
    public void testImportTopTen_FR0007052782_AddWithSimilarName() throws IOException, ParserConfigurationException, SAXException {
        // "LVMH MOET HENNESSY LOUIS VUI" to add 1154
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-IE000CNSFAR2.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        List<Security> securities = new ArrayList<>(2);
        Security security = service.createSecurity("FR0007052782", 0);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(10, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        securities.add(security);
        security = service.createSecurity("IE00B945VV12", 1);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(10, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Unternehmensgewichtung")) {
                    Element lvmh = portfolioDocumentService.findClassificationByName(taxonomyElement, "LVMH Moet Hennessy Louis Vuitton SE");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(lvmh, 1);
                    assertNull(assignment);
                    Element lvmhNew = portfolioDocumentService.findClassificationByName(taxonomyElement, "LVMH MOET HENNESSY LOUIS VUI");
                    assertNull(lvmhNew);
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(lvmh, 2);
                    assertNotNull(assignment);
                    assertEquals("190", getWeightOfAssignment(assignment));

                    JsonArray importedBranches = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, taxonomyElement);
                    assertEquals(10, importedBranches.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(lvmh, 1);
                    assertNotNull(assignment);
                    assertEquals("1154", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(lvmh, 2);
                    assertNotNull(assignment);
                    assertEquals("190", getWeightOfAssignment(assignment));
                    lvmhNew = portfolioDocumentService.findClassificationByName(taxonomyElement, "LVMH MOET HENNESSY LOUIS VUI");
                    assertNull(lvmhNew);
                }
            }
        }
    }

    @Test
    public void testImportTopTen_FR0007052782_AddWithSimilarNameReverseOrder() throws Exception {
        // "LVMH MOET HENNESSY LOUIS VUI" to add 1154
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-FR0007052782.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        List<Security> securities = new ArrayList<>(2);
        Security security = service.createSecurity("IE00B945VV12", 0);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(10, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        securities.add(security);
        security = service.createSecurity("FR0007052782", 1);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(10, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Unternehmensgewichtung")) {
                    // existing
                    Element lvmh = portfolioDocumentService.findClassificationByName(taxonomyElement, "LVMH Moet Hennessy Louis Vuitton SE");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(lvmh, 1);
                    assertNotNull(assignment);
                    assertEquals("190", getWeightOfAssignment(assignment));
                    // not existing yet
                    Element lvmhNew = portfolioDocumentService.findClassificationByName(taxonomyElement, "LVMH MOET HENNESSY LOUIS VUI");
                    assertNull(lvmhNew);
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(lvmh, 2);
                    assertNull(assignment);

                    JsonArray importedBranches = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, taxonomyElement);
                    assertEquals(10, importedBranches.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(lvmh, 1);
                    assertNotNull(assignment);
                    assertEquals("190", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(lvmh, 2);
                    assertNotNull(assignment);
                    assertEquals("1154", getWeightOfAssignment(assignment));
                    lvmhNew = portfolioDocumentService.findClassificationByName(taxonomyElement, "LVMH MOET HENNESSY LOUIS VUI");
                    assertNull(lvmhNew);
                    logger.info(xmlHelper.domNode2String(lvmh, true));
                }
            }
        }
    }

    @Test
    public void testImportTopTen_IE000CNSFAR2_Update() throws IOException, ParserConfigurationException, SAXException {
        // "Meta Platforms Inc." to update 172
        // "Microsoft" to update 462
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-IE000CNSFAR2.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(10, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        List<Security> securities = new ArrayList<>(1);
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Unternehmensgewichtung")) {
                    Element amazon = portfolioDocumentService.findClassificationByName(taxonomyElement, "Amazon");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(amazon, 1);
                    assertNotNull(assignment);
                    assertEquals("260", getWeightOfAssignment(assignment));
                    Element meta = portfolioDocumentService.findClassificationByName(taxonomyElement, "Meta Platforms Inc.");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(meta, 1);
                    assertNotNull(assignment);
                    assertEquals("100", getWeightOfAssignment(assignment));
                    Element microsoft = portfolioDocumentService.findClassificationByName(taxonomyElement, "Microsoft");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(microsoft, 1);
                    assertNotNull(assignment);
                    assertEquals("200", getWeightOfAssignment(assignment));

                    JsonArray importedBranches = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, taxonomyElement);
                    assertEquals(3, importedBranches.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(amazon, 1);
                    assertNotNull(assignment);
                    assertEquals("260", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(meta, 1);
                    assertNotNull(assignment);
                    assertEquals("172", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(microsoft, 1);
                    assertNotNull(assignment);
                    assertEquals("462", getWeightOfAssignment(assignment));
                }
            }
        }
    }

    @Test
    public void testImportTopTen_AlphabetAdd2ExistingClassification() throws Exception {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-Alphabet2.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        List<Security> securities = new ArrayList<>(2);
        Security security = service.createSecurity("LU1681043599-Alphabet", 0);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(2, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        securities.add(security);
        security = service.createSecurity("IE000CNSFAR2-Alphabet", 1);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(2, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Unternehmensgewichtung")) {
                    Element alphabet = portfolioDocumentService.findClassificationByName(taxonomyElement, "Alphabet A (Google)");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(alphabet, 1);
                    assertNull(assignment);
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(alphabet, 2);
                    assertNull(assignment);

                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, taxonomyElement);
                    assertEquals(4, importedTopTen.size());

                    List<Element> foundAssignments = portfolioDocumentService.findAssignmentsBySecurityIndex(alphabet, 1);
                    assertFalse(foundAssignments.isEmpty());
                    assertEquals(2, foundAssignments.size());
                    foundAssignments = portfolioDocumentService.findAssignmentsBySecurityIndex(alphabet, 2);
                    assertFalse(foundAssignments.isEmpty());
                    assertEquals(2, foundAssignments.size());
                    logger.info(xmlHelper.domNode2String(alphabet, true));
                }
            }
        }
    }

    @Test
    public void testImportTopTen_AlphabetAdd2NewClassification() throws Exception {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-Alphabet3.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        List<Security> securities = new ArrayList<>(2);
        Security security = service.createSecurity("LU1681043599-Alphabet", 0);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(2, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        securities.add(security);
        security = service.createSecurity("IE000CNSFAR2-Alphabet", 1);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(2, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Unternehmensgewichtung")) {
                    Element alphabet = portfolioDocumentService.findClassificationByName(taxonomyElement, "Alphabet A (Google)");
                    assertNull(alphabet);

                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, taxonomyElement);
                    assertEquals(4, importedTopTen.size());

                    alphabet = portfolioDocumentService.findClassificationByName(taxonomyElement, "Alphabet A (Google)");
                    List<Element> foundAssignments = portfolioDocumentService.findAssignmentsBySecurityIndex(alphabet, 1);
                    assertFalse(foundAssignments.isEmpty());
                    assertEquals(2, foundAssignments.size());
                    foundAssignments = portfolioDocumentService.findAssignmentsBySecurityIndex(alphabet, 2);
                    assertFalse(foundAssignments.isEmpty());
                    assertEquals(2, foundAssignments.size());
                    logger.info(xmlHelper.domNode2String(alphabet, true));
                }
            }
        }
    }

    @Test
    public void testImportTopTen_AlphabetAdd2ExistingClassificationSecondHolding() throws Exception {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-Alphabet4.xml");
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        List<Security> securities = new ArrayList<>(2);
        Security security = service.createSecurity("LU1681043599-Alphabet", 0);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(2, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        securities.add(security);
        security = service.createSecurity("IE000CNSFAR2-Alphabet", 1);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(2, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        securities.add(security);
        security = service.createSecurity("DE000A0F5UF5-Alphabet", 2);
        assertNotNull(security);
        assertNotNull(security.getHoldings());
        assertEquals(2, security.getHoldings().size());
        logger.info("Holdings from Security: " + security.getHoldings().keySet().stream().sorted().collect(Collectors.toList()));
        securities.add(security);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals("Unternehmensgewichtung")) {
                    Element alphabet = portfolioDocumentService.findClassificationByName(taxonomyElement, "Alphabet A (Google)");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(alphabet, 1);
                    assertNotNull(assignment);
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(alphabet, 2);
                    assertNotNull(assignment);

                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, taxonomyElement);
                    assertEquals(4, importedTopTen.size());
                    logger.info(xmlHelper.domNode2String(alphabet, true));

                    List<Element> foundAssignments = portfolioDocumentService.findAssignmentsBySecurityIndex(alphabet, 1);
                    assertFalse(foundAssignments.isEmpty());
                    assertEquals(2, foundAssignments.size());
                    assertEquals("311", getWeightOfAssignment(foundAssignments.get(1)));
                    assertEquals("322", getWeightOfAssignment(foundAssignments.get(0)));
                    foundAssignments = portfolioDocumentService.findAssignmentsBySecurityIndex(alphabet, 3);
                    assertFalse(foundAssignments.isEmpty());
                    assertEquals(2, foundAssignments.size());
                    assertEquals("211", getWeightOfAssignment(foundAssignments.get(0)));
                    assertEquals("222", getWeightOfAssignment(foundAssignments.get(1)));
                    foundAssignments = portfolioDocumentService.findAssignmentsBySecurityIndex(alphabet, 2);
                    assertFalse(foundAssignments.isEmpty());
                    assertEquals(2, foundAssignments.size());
                    assertEquals("111", getWeightOfAssignment(foundAssignments.get(0)));
                    assertEquals("122", getWeightOfAssignment(foundAssignments.get(1)));
                }
            }
        }
    }

}
