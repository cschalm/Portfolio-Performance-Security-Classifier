package org.schalm.ppsc.services;

import com.google.gson.JsonArray;
import org.junit.Test;
import org.schalm.ppsc.models.*;
import org.schalm.ppsc.xml.XmlFileWriter;
import org.schalm.ppsc.xml.XmlHelper;
import org.schalm.test.AbstractTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.schalm.ppsc.constants.PathConstants.BASE_TARGET_PATH;
import static org.schalm.ppsc.services.PortfolioDocumentService.*;

public class PortfolioDocumentServiceTest extends AbstractTest {
    private static final Logger logger = Logger.getLogger(PortfolioDocumentServiceTest.class.getCanonicalName());
    final String LVMH_ONE = "LVMH Moet Hennessy Louis Vuitton SE";
    final String LVMH_TWO = "LVMH MOET HENNESSY LOUIS VUI";
    XmlHelper xmlHelper = new XmlHelper();
    PortfolioDocumentService portfolioDocumentService = new PortfolioDocumentService();

    private List<Security> createTestSecurityEtfIE00BYYHSM20() {
        Security security = new ETF("IE00BYYHSM20", 1, true);
        Map<String, Double> holdings = security.getHoldings();
        holdings.put("ABB", 2.88);
        holdings.put("Novo Nordisk", 4.83);
        holdings.put("SAP", 4.0);
        holdings.put("Lonza Group", 2.72);
        holdings.put("Zurich Insurance Group", 2.68);
        holdings.put("AXA", 2.67);
        holdings.put("Diageo", 2.71);
        holdings.put("Schneider Electric", 3.43);
        holdings.put("Relx Plc", 2.89);
        holdings.put("ASML Holding", 4.29);
        Map<String, Double> industries = security.getIndustries();
        industries.put("Industrie", 14.06);
        industries.put("Informationstechnologie", 11.61);
        industries.put("Versorgungsbetriebe", 9.24);
        industries.put("Gesundheitswesen", 8.32);
        industries.put("Roh-, Hilfs- & Betriebsstoffe", 6.63);
        industries.put("Energie", 2.27);
        industries.put("Finanzwesen", 27.91);
        industries.put("Basiskonsumgüter", 15.06);
        industries.put("Immobilien", 3.77);
        Map<String, Double> countries = security.getCountries();
        countries.put("Großbritannien", 19.35);
        countries.put("Belgien", 19.35);
        countries.put("Norwegen", 19.35);
        countries.put("Spanien", 19.35);
        countries.put("Österreich", 19.35);
        countries.put("Dänemark", 19.35);
        countries.put("Schweiz", 19.35);
        countries.put("Deutschland", 19.35);
        countries.put("Frankreich", 19.35);
        countries.put("Niederlande", 19.35);
        countries.put("Finnland", 19.35);
        countries.put("Schweden", 19.35);
        countries.put("Italien", 19.35);
        countries.put("Irland", 19.35);
        return List.of(security);
    }

    private List<Security> createTestSecurityEtfIE000CNSFAR2() {
        Security security = new ETF("IE000CNSFAR2", 1, true);
        addDetails(security);
        return List.of(security);
    }

    private List<Security> createTestSecurityEtfIE000CNSFAR2Inactive() {
        Security security = new ETF("IE000CNSFAR2", 1, false);
        addDetails(security);
        return List.of(security);
    }

    private static void addDetails(Security security) {
        Map<String, Double> holdings = security.getHoldings();
        holdings.put("Meta Platforms (ehem. Facebook)", 1.72);
        holdings.put("Nvidia", 3.09);
        holdings.put("Apple", 4.2);
        holdings.put("Alphabet A (Google)", 1.3);
        holdings.put("Tesla", 0.91);
        holdings.put("Microsoft", 4.62);
        holdings.put("Alphabet C (Google)", 1.14);
        holdings.put("Amazon", 2.6);
        holdings.put("Broadcom", 0.91);
        holdings.put("Eli Lilly and Company", 0.96);
        Map<String, Double> industries = security.getIndustries();
        industries.put("Industrie", 11.13);
        industries.put("Informationstechnologie", 24.11);
        industries.put("Telekommunikationsdienste", 7.42);
        industries.put("Gesundheitswesen", 12.05);
        industries.put("Nicht-Basiskonsumgüter", 10.92);
        industries.put("Roh-, Hilfs- & Betriebsstoffe", 3.8);
        industries.put("Versorgungsbetriebe", 2.37);
        industries.put("Energie", 4.24);
        industries.put("Finanzwesen", 15.12);
        industries.put("Basiskonsumgüter", 6.55);
        industries.put("Immobilien", 2.28);
        Map<String, Double> countries = security.getCountries();
        countries.put("Großbritannien", 2.78);
        countries.put("USA", 72.43);
        countries.put("Kanada", 3.37);
        countries.put("Belgien", 0.28);
        countries.put("Singapur", 0.4);
        countries.put("Norwegen", 0.16);
        countries.put("Japan", 5.71);
        countries.put("Spanien", 0.93);
        countries.put("Hongkong", 0.46);
        countries.put("Österreich", 0.08);
        countries.put("Portugal", 0.05);
        countries.put("Dänemark", 0.41);
        countries.put("Schweiz", 2.25);
        countries.put("Neuseeland", 0.05);
        countries.put("Australien", 1.59);
        countries.put("Frankreich", 2.39);
        countries.put("Deutschland", 2.18);
        countries.put("Niederlande", 1.37);
        countries.put("Finnland", 0.31);
        countries.put("Schweden", 0.87);
        countries.put("Israel", 0.28);
        countries.put("Italien", 0.8);
        countries.put("Irland", 0.11);
    }

    private List<Security> createTestSecurityEtfLU1681043599Alphabet() {
        Security security = new ETF("LU1681043599-Alphabet", 1, true);
        Map<String, Double> holdings = security.getHoldings();
        holdings.put("ALPHABET INC CL A", 3.22);
        holdings.put("ALPHABET INC CL C", 3.11);
        return List.of(security);
    }

    private List<Security> createTestSecurityEtfIE000CNSFAR2Alphabet() {
        Security security = new ETF("IE000CNSFAR2-Alphabet", 2, true);
        Map<String, Double> holdings = security.getHoldings();
        holdings.put("Alphabet A (Google)", 1.11);
        holdings.put("Alphabet C (Google)", 1.22);
        return List.of(security);
    }

    private List<Security> createTestSecurityEtfDE000A0F5UF5Alphabet() {
        Security security = new ETF("DE000A0F5UF5-Alphabet", 3, true);
        Map<String, Double> holdings = security.getHoldings();
        holdings.put("Alphabet A (Google)", 2.11);
        holdings.put("Alphabet C (Google)", 2.22);
        return List.of(security);
    }

    private List<Security> createTestSecurityEtfFR0007052782LVMH() {
        Security security = new ETF("FR0007052782", 1, true);
        Map<String, Double> holdings = security.getHoldings();
        holdings.put(LVMH_TWO, 11.54);
        return List.of(security);
    }

    private List<Security> createTestSecurityEtfIE00B945VV12LVMH() {
        Security security = new ETF("IE00B945VV12", 2, true);
        Map<String, Double> holdings = security.getHoldings();
        holdings.put(LVMH_ONE, 1.9);
        return List.of(security);
    }

    private List<Security> createTestSecurityCommodityGold() {
        Security security = new Commodity("XC0009655157", 1, true);
        return List.of(security);
    }

    @Test
    public void testUpdateXml_ETF_IE00BYYHSM20() throws Exception {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        List<Security> securities = createTestSecurityEtfIE00BYYHSM20();

        SecurityDetailsCache securityDetailsCache = new SecurityDetailsCache(BASE_TARGET_PATH + "test-classes/IE00BYYHSM20-" + UUID.randomUUID() + ".json");

        portfolioDocumentService.updateXml(portfolioDocument, securities, securityDetailsCache);
        assertEquals("Countries", 14, securityDetailsCache.getCachedCountries().asList().size());
        assertEquals("Branches", 9, securityDetailsCache.getCachedIndustries().asList().size());
        assertEquals("Top 10", 10, securityDetailsCache.getCachedTopTen().asList().size());
    }

    @Test
    public void testUpdateXml_ETF_IE000CNSFAR2() throws Exception {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2();
        SecurityDetailsCache securityDetailsCache = new SecurityDetailsCache(BASE_TARGET_PATH + "test-classes/IE000CNSFAR2-" + UUID.randomUUID() + ".json");

        portfolioDocumentService.updateXml(portfolioDocument, securities, securityDetailsCache);
        assertEquals("Countries", 23, securityDetailsCache.getCachedCountries().asList().size());
        assertEquals("Branches", 11, securityDetailsCache.getCachedIndustries().asList().size());
        assertEquals("Top 10", 10, securityDetailsCache.getCachedTopTen().asList().size());
    }

    @Test
    public void testUpdateXml_inactiveETF_mustNotImportAnything() throws Exception {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        List<Security> securities = List.of(new ETF("IE000CNSFAR2", 0, false));
        SecurityDetailsCache securityDetailsCache = new SecurityDetailsCache(BASE_TARGET_PATH + "test-classes/IE000CNSFAR2-" + UUID.randomUUID() + ".json");

        portfolioDocumentService.updateXml(portfolioDocument, securities, securityDetailsCache);
        assertEquals("Countries", 0, securityDetailsCache.getCachedCountries().asList().size());
        assertEquals("Branches", 0, securityDetailsCache.getCachedIndustries().asList().size());
        assertEquals("Top 10", 0, securityDetailsCache.getCachedTopTen().asList().size());
    }

    @Test
    public void testUpdateXml_CommodityGold() throws Exception {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single Commodity.xml");
        List<Security> securities = List.of(new Commodity("XC0009655157", 0, true));
        SecurityDetailsCache securityDetailsCache = new SecurityDetailsCache(BASE_TARGET_PATH + "test-classes/XC0009655157-" + UUID.randomUUID() + ".json");

        portfolioDocumentService.updateXml(portfolioDocument, securities, securityDetailsCache);
        assertEquals("Countries", 0, securityDetailsCache.getCachedCountries().asList().size());
        assertEquals("Branches", 0, securityDetailsCache.getCachedIndustries().asList().size());
        assertEquals("Top 10", 0, securityDetailsCache.getCachedTopTen().asList().size());

        XmlFileWriter xmlFileWriter = new XmlFileWriter();
        xmlFileWriter.writeXml(portfolioDocument, BASE_TEST_PATH + "Portfolio Performance Single Commodity-Result.xml");
    }

    @Test
    public void testImportIndustries_IE00BYYHSM20() throws Exception {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        List<Security> securities = createTestSecurityEtfIE00BYYHSM20();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_INDUSTRIES_GICS)) {
                    JsonArray importedBranches = portfolioDocumentService.importIndustries(portfolioDocument, securities, taxonomyElement);
                    assertEquals(9, importedBranches.size());
                }
            }
        }
    }

    @Test
    public void testImportIndustries_IE000CNSFAR2() throws Exception {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_INDUSTRIES_GICS)) {
                    JsonArray importedBranches = portfolioDocumentService.importIndustries(portfolioDocument, securities, taxonomyElement);
                    assertEquals(11, importedBranches.size());
                }
            }
        }
    }

    @Test
    public void testImportTopTen_IE000CNSFAR2() throws Exception {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_TOPTEN)) {
                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, securities, taxonomyElement);
                    assertEquals(10, importedTopTen.size());
                }
            }
        }
    }

    @Test
    public void testImportTopTen_IE00BYYHSM20() throws Exception {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        List<Security> securities = createTestSecurityEtfIE00BYYHSM20();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_TOPTEN)) {
                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, securities, taxonomyElement);
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
    public void testImportRegions_IE000CNSFAR2() throws Exception {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "Portfolio Performance Single.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_REGIONS)) {
                    JsonArray importedCountries = portfolioDocumentService.importRegions(portfolioDocument, securities, taxonomyElement);
                    assertEquals(23, importedCountries.size());
                }
            }
        }
    }

    @Test
    public void testCollectAllStockNames() {
        List<Security> securities = createTestSecurityEtfIE00BYYHSM20();
        TreeMap<String, List<String>> allStockNames = portfolioDocumentService.collectAllStockNames(securities);
        assertNotNull(allStockNames);
        assertEquals(10, allStockNames.size());
    }

    @Test
    public void testCollectAllStockNamesEtf() throws Exception {
        Security msciWorldEtfT = new ETF("LU1681043599", 0, true);
        Map<String, Double> holdings = msciWorldEtfT.getHoldings();
        holdings.put("Meta Platforms Inc", 1d);

        Security msciWorldEtfD = new ETF("IE000CNSFAR2", 1, true);
        holdings = msciWorldEtfD.getHoldings();
        holdings.put("Meta Platforms Inc Class A", 2d);

        Security msciUSA2x = new ETF("FR0010755611", 2, true);
        holdings = msciUSA2x.getHoldings();
        holdings.put("Meta Platform", 3d);

        Security nasdaq100 = new ETF("DE000A0F5UF5", 3, true);
        holdings = nasdaq100.getHoldings();
        holdings.put("Meta Platforms Inc.", 4d);

        Security ftseNorthAmerica = new ETF("IE00BKX55R35", 4, true);
        holdings = ftseNorthAmerica.getHoldings();
        holdings.put("Meta Platforms ex Facebook", 5d);

        List<Security> allSecurities = new ArrayList<>(5);
        allSecurities.add(msciWorldEtfD);
        allSecurities.add(msciWorldEtfT);
        allSecurities.add(msciUSA2x);
        allSecurities.add(nasdaq100);
        allSecurities.add(ftseNorthAmerica);

        TreeMap<String, List<String>> allStockNames = portfolioDocumentService.collectAllStockNames(allSecurities);
        assertNotNull(allStockNames);
        assertFalse(allStockNames.isEmpty());
        assertEquals(1, allStockNames.size());
        assertEquals(4, allStockNames.entrySet().iterator().next().getValue().size());
    }

    @Test
    public void testCollectAllStockNamesCommodity() {
        List<Security> securities = createTestSecurityCommodityGold();
        TreeMap<String, List<String>> allStockNames = portfolioDocumentService.collectAllStockNames(securities);
        assertNotNull(allStockNames);
        assertEquals(0, allStockNames.size());
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
    public void testIsNameSimilar() {
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
        assertEquals("Banken", portfolioDocumentService.optimizeIndustryNameFromSecurity("Banken", "DE000TUAG505"));
        assertEquals("Basiskonsumgüter", portfolioDocumentService.optimizeIndustryNameFromSecurity("Basiskonsumgüter", "DE000TUAG505"));
        assertEquals("Baumaterialien", portfolioDocumentService.optimizeIndustryNameFromSecurity("Baumaterialien/Baukomponenten", "DE000TUAG505"));
        assertEquals("Chemikalien", portfolioDocumentService.optimizeIndustryNameFromSecurity("Chemikalien", "DE000TUAG505"));
        assertEquals("Hardware Technologie, Speicherung & Peripherie", portfolioDocumentService.optimizeIndustryNameFromSecurity("Computerherstellung", "DE000TUAG505"));
        assertEquals("Verschiedene REITs", portfolioDocumentService.optimizeIndustryNameFromSecurity("Diversifizierte REITs", "DE000TUAG505"));
        assertEquals("Drahtlose Telekommunikationsdienste", portfolioDocumentService.optimizeIndustryNameFromSecurity("Drahtlose Telekommunikationsdienste", "DE000TUAG505"));
        assertEquals("Handels-REITs", portfolioDocumentService.optimizeIndustryNameFromSecurity("Einzelhandel REITs", "DE000TUAG505"));
        assertEquals("Elektronische Komponenten", portfolioDocumentService.optimizeIndustryNameFromSecurity("Elektrokomponenten", "DE000TUAG505"));
        assertEquals("Elektronische Geräte & Instrumente", portfolioDocumentService.optimizeIndustryNameFromSecurity("Elektrokomponenten & -geräte", "DE000TUAG505"));
        assertEquals("Energie", portfolioDocumentService.optimizeIndustryNameFromSecurity("Energie", "DE000TUAG505"));
        assertEquals("Automobilbranche", portfolioDocumentService.optimizeIndustryNameFromSecurity("Fahrzeugbau", "DE000TUAG505"));
        assertEquals("Automobilbranche", portfolioDocumentService.optimizeIndustryNameFromSecurity("Kraftfahrzeuge", "DE000TUAG505"));
        assertEquals("Private Finanzdienste", portfolioDocumentService.optimizeIndustryNameFromSecurity("Finanzdienstleistungen", "DE000TUAG505"));
        assertEquals("Finanzwesen", portfolioDocumentService.optimizeIndustryNameFromSecurity("Finanzen", "DE000TUAG505"));
        assertEquals("Gesundheitswesen", portfolioDocumentService.optimizeIndustryNameFromSecurity("Gesundheitswesen", "DE000TUAG505"));
        assertEquals("Halbleiter", portfolioDocumentService.optimizeIndustryNameFromSecurity("Halbleiterelektronik", "DE000TUAG505"));
        assertEquals("Hardware Technologie, Speicherung & Peripherie", portfolioDocumentService.optimizeIndustryNameFromSecurity("Hardware- Technologie, Speicherung und Peripheriegeräte", "DE000TUAG505"));
        assertEquals("Hotels, Restaurants und Freizeit", portfolioDocumentService.optimizeIndustryNameFromSecurity("Hotels, Restaurants und Freizeit", "DE000TUAG505"));
        assertEquals("Hypotheken-, Immobilien-, Investment-, Trusts (REITs)", portfolioDocumentService.optimizeIndustryNameFromSecurity("Hypotheken-Immobilien-fonds (REITs)", "DE000TUAG505"));
        assertEquals("Informationstechnologie", portfolioDocumentService.optimizeIndustryNameFromSecurity("IT/Telekommunikation", "DE000TUAG505"));
        assertEquals("Immobilien", portfolioDocumentService.optimizeIndustryNameFromSecurity("Immobilien", "DE000TUAG505"));
        assertEquals("Industrie", portfolioDocumentService.optimizeIndustryNameFromSecurity("Industrie", "DE000TUAG505"));
        assertEquals("Industriemaschinen", portfolioDocumentService.optimizeIndustryNameFromSecurity("Industriemaschinenbau", "DE000TUAG505"));
        assertEquals("Informationstechnologie", portfolioDocumentService.optimizeIndustryNameFromSecurity("Informationstechnologie", "DE000TUAG505"));
        assertEquals("Basiskonsumgüter", portfolioDocumentService.optimizeIndustryNameFromSecurity("Konsumgüter", "DE000TUAG505"));
        assertEquals("Nicht-Basiskonsumgüter", portfolioDocumentService.optimizeIndustryNameFromSecurity("Konsumgüter zyklisch", "DE000TUAG505"));
        assertEquals("Roh-, Hilfs- & Betriebsstoffe", portfolioDocumentService.optimizeIndustryNameFromSecurity("Rohstoffe", "DE000TUAG505"));
        assertEquals("Rückversicherung", portfolioDocumentService.optimizeIndustryNameFromSecurity("Rückversicherung", "DE000TUAG505"));
        assertEquals("Rückversicherungen", portfolioDocumentService.optimizeIndustryNameFromSecurity("Rückversicherung", "DE0008402215"));
        assertEquals("Software", portfolioDocumentService.optimizeIndustryNameFromSecurity("Software", "DE000TUAG505"));
        assertEquals("Telekommunikationsdienste", portfolioDocumentService.optimizeIndustryNameFromSecurity("Telekomdienste", "DE000TUAG505"));
        assertEquals("Telekommunikationsdienste", portfolioDocumentService.optimizeIndustryNameFromSecurity("Telekommunikation", "DE000TUAG505"));
        assertEquals("Verbraucherelektronik", portfolioDocumentService.optimizeIndustryNameFromSecurity("Verbraucherelektronik", "DE000TUAG505"));
        assertEquals("Versicherung", portfolioDocumentService.optimizeIndustryNameFromSecurity("Versicherung", "DE000TUAG505"));
        assertEquals("Versorgungsbetriebe", portfolioDocumentService.optimizeIndustryNameFromSecurity("Versorger", "DE000TUAG505"));
        assertEquals("", portfolioDocumentService.optimizeIndustryNameFromSecurity("diverse Branchen", "DE000TUAG505"));
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
    public void testFindAssignmentBySecurityIndex() throws Exception {
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
    public void testUpdateWeightOfAssignmentCountry() throws Exception {
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
    public void testImportRegions_IE000CNSFAR2_Remove() throws Exception {
        // "Tschechien" to be removed by import
        // "Ungarn" to be removed by import
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-country-IE000CNSFAR2.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_REGIONS)) {
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
    public void testImportRegions_IE000CNSFAR2_RemoveInactive() throws Exception {
        // "Finnland" to be removed by import
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-country-IE000CNSFAR2.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2Inactive();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_REGIONS)) {
                    Element finnland = portfolioDocumentService.findClassificationByName(taxonomyElement, "Finnland");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(finnland, 1);
                    assertNotNull(assignment);

                    JsonArray importedCountries = portfolioDocumentService.importRegions(portfolioDocument, securities, taxonomyElement);
                    assertEquals(0, importedCountries.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(finnland, 1);
                    assertNull(assignment);
                }
            }
        }
    }

    @Test
    public void testImportRegions_IE000CNSFAR2_Add() throws Exception {
        // "Italien" to add
        // "Portugal" to add
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-country-IE000CNSFAR2.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_REGIONS)) {
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
                    assertEquals("80", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(portugal, 1);
                    assertNotNull(assignment);
                    assertEquals("5", getWeightOfAssignment(assignment));
                }
            }
        }
    }

    @Test
    public void testImportRegions_IE000CNSFAR2_Update() throws Exception {
        // "Dänemark" to update
        // "Finnland" to update
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-country-IE000CNSFAR2.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_REGIONS)) {
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
                    assertEquals("41", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(finnland, 1);
                    assertNotNull(assignment);
                    assertEquals("31", getWeightOfAssignment(assignment));
                }
            }
        }
    }

    private String getWeightOfAssignment(Element assignment) {
        return xmlHelper.getTextContent(assignment, "weight");
    }

    @Test
    public void testUpdateWeightOfAssignmentIndustry() throws Exception {
        Document document = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-industry.xml");
        Node classification = document.getFirstChild();
        Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(classification, 38);
        assertNotNull(assignment);
        String weight = xmlHelper.getTextContent(assignment, "weight");
        assertNotNull(weight);
        assertEquals("37", weight);

        assignment = portfolioDocumentService.updateWeightOfAssignment(assignment, "408");
        assertNotNull(assignment);
        weight = xmlHelper.getTextContent(assignment, "weight");
        assertNotNull(weight);
        assertEquals("408", weight);
    }

    @Test
    public void testImportIndustries_IE000CNSFAR2_Remove() throws Exception {
        // "Kapitalmärkte" to be removed by import
        // "Software" to be removed by import
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-industry-IE000CNSFAR2.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_INDUSTRIES_GICS)) {
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
                    String weight = xmlHelper.getTextContent(assignment, "weight");
                    assertNotNull(weight);
                    assertEquals("2411", weight);

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(kapitalmaerkte, 1);
                    assertNull(assignment);

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(software, 1);
                    assertNull(assignment);
                }
            }
        }
    }

    @Test
    public void testImportIndustries_IE000CNSFAR2_RemoveInactive() throws Exception {
        // "Kapitalmärkte" to be removed by import
        // "Software" to be removed by import
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-industry-IE000CNSFAR2.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2Inactive();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_INDUSTRIES_GICS)) {
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
                    assertEquals(0, importedBranches.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(informationstechnologie, 1);
                    assertNull(assignment);

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(kapitalmaerkte, 1);
                    assertNull(assignment);

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(software, 1);
                    assertNull(assignment);
                }
            }
        }
    }

    @Test
    public void testImportIndustries_IE000CNSFAR2_Add() throws Exception {
        // "Nicht-Basiskonsumgüter" to add 1092
        // "Basiskonsumgüter" to add 655
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-industry-IE000CNSFAR2.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_INDUSTRIES_GICS)) {
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
    public void testImportIndustries_IE000CNSFAR2_Update() throws Exception {
        // "Gesundheitswesen" to update 1205
        // "Industrie" to update 1113
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-industry-IE000CNSFAR2.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_INDUSTRIES_GICS)) {
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
    public void testUpdateWeightOfAssignmentTopTen() throws Exception {
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
    public void testImportTopTen_IE000CNSFAR2_Remove() throws Exception {
        // "ABB" to be removed by import
        // "Saia" to be removed by import
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-IE000CNSFAR2.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_TOPTEN)) {
                    Element abb = portfolioDocumentService.findClassificationByName(taxonomyElement, "ABB");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(abb, 1);
                    assertNotNull(assignment);
                    Element saia = portfolioDocumentService.findClassificationByName(taxonomyElement, "Saia");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(saia, 1);
                    assertNotNull(assignment);
                    Element nvidia = portfolioDocumentService.findClassificationByName(taxonomyElement, "NVIDIA Corp.");
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(nvidia, 1);
                    assertNotNull(assignment);

                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, securities, taxonomyElement);
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
    public void testImportTopTen_IE000CNSFAR2_RemoveOnlyOneEntry() throws Exception {
        // nothing to be removed by import
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-IE000CNSFAR2.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_TOPTEN)) {
                    Element tesla = portfolioDocumentService.findClassificationByName(taxonomyElement, "Tesla");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(tesla, 1);
                    assertNotNull(assignment);

                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, securities, taxonomyElement);
                    assertEquals(3, importedTopTen.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(tesla, 1);
                    assertNotNull(assignment);
                }
            }
        }
    }

    @Test
    public void testImportTopTen_IE000CNSFAR2_RemoveOnlyOneEntryAndClassificationFolder() throws Exception {
        // nothing to be removed by import
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-IE000CNSFAR2.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_TOPTEN)) {
                    Element amd = portfolioDocumentService.findClassificationByName(taxonomyElement, "AMD");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(amd, 1);
                    assertNotNull(assignment);

                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, securities, taxonomyElement);
                    assertEquals(3, importedTopTen.size());

                    amd = portfolioDocumentService.findClassificationByName(taxonomyElement, "AMD");
                    assertNull(amd);
                }
            }
        }
    }

    @Test
    public void testImportTopTen_IE000CNSFAR2_Add() throws Exception {
        // "Alphabet A (Google)" to add 130
        // "Eli Lilly & Co." to add 96
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-IE000CNSFAR2.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_TOPTEN)) {
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

                    JsonArray importedBranches = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, securities, taxonomyElement);
//                    XmlFileWriter xmlFileWriter = new XmlFileWriter();
//                    xmlFileWriter.writeXml(portfolioDocument, BASE_TEST_PATH + "classification-topten-IE000CNSFAR2-RESULT.xml");
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
    public void testImportTopTen_FR0007052782_AddWithSimilarName() throws Exception {
        // "LVMH MOET HENNESSY LOUIS VUI" to add 1154
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-IE000CNSFAR2.xml");
        List<Security> securities = new ArrayList<>();
        securities.addAll(createTestSecurityEtfFR0007052782LVMH());
        securities.addAll(createTestSecurityEtfIE00B945VV12LVMH());

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_TOPTEN)) {
                    Element lvmh = portfolioDocumentService.findClassificationByName(taxonomyElement, LVMH_ONE);
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(lvmh, 1);
                    assertNull(assignment);
                    Element lvmhNew = portfolioDocumentService.findClassificationByName(taxonomyElement, LVMH_TWO);
                    assertNull(lvmhNew);
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(lvmh, 2);
                    assertNotNull(assignment);
                    assertEquals("190", getWeightOfAssignment(assignment));

                    JsonArray importedBranches = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, securities, taxonomyElement);
                    assertEquals(1, importedBranches.size());

                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(lvmh, 1);
                    assertNotNull(assignment);
                    assertEquals("1154", getWeightOfAssignment(assignment));
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(lvmh, 2);
                    assertNotNull(assignment);
                    assertEquals("190", getWeightOfAssignment(assignment));
                    lvmhNew = portfolioDocumentService.findClassificationByName(taxonomyElement, LVMH_TWO);
                    assertNull(lvmhNew);
                }
            }
        }
    }

    @Test
    public void testImportTopTen_FR0007052782_AddWithSimilarNameReverseOrder() throws Exception {
        // "LVMH MOET HENNESSY LOUIS VUI" to add 1154
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-FR0007052782.xml");
        List<Security> securities = new ArrayList<>();
        securities.addAll(createTestSecurityEtfFR0007052782LVMH());
        securities.addAll(createTestSecurityEtfIE00B945VV12LVMH());

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_TOPTEN)) {
                    // existing
                    Element lvmh = portfolioDocumentService.findClassificationByName(taxonomyElement, "LVMH Moet Hennessy Louis Vuitton SE");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(lvmh, 2);
                    assertNotNull(assignment);
                    assertEquals("190", getWeightOfAssignment(assignment));
                    // not existing yet
                    Element lvmhNew = portfolioDocumentService.findClassificationByName(taxonomyElement, "LVMH MOET HENNESSY LOUIS VUI");
                    assertNull(lvmhNew);
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(lvmh, 1);
                    assertNull(assignment);

                    JsonArray importedBranches = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, securities, taxonomyElement);
                    logger.info(xmlHelper.domNode2String(lvmh, true));
                    assertEquals(1, importedBranches.size());

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
    public void testImportTopTen_IE000CNSFAR2_Update() throws Exception {
        // "Meta Platforms Inc." to update 172
        // "Microsoft" to update 462
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-IE000CNSFAR2.xml");
        List<Security> securities = createTestSecurityEtfIE000CNSFAR2();

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_TOPTEN)) {
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

                    JsonArray importedBranches = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, securities, taxonomyElement);
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
        List<Security> securities = new ArrayList<>();
        securities.addAll(createTestSecurityEtfLU1681043599Alphabet());
        securities.addAll(createTestSecurityEtfIE000CNSFAR2Alphabet());

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_TOPTEN)) {
                    Element alphabet = portfolioDocumentService.findClassificationByName(taxonomyElement, "Alphabet A (Google)");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(alphabet, 1);
                    assertNull(assignment);
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(alphabet, 2);
                    assertNull(assignment);

                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, securities, taxonomyElement);
                    assertEquals(4, importedTopTen.size());

                    List<Element> foundAssignments = portfolioDocumentService.findAssignmentsBySecurityIndex(alphabet, 1);
                    assertFalse(foundAssignments.isEmpty());
                    assertEquals(2, foundAssignments.size());
                    foundAssignments = portfolioDocumentService.findAssignmentsBySecurityIndex(alphabet, 2);
                    assertFalse(foundAssignments.isEmpty());
                    assertEquals(2, foundAssignments.size());
//                    logger.info(xmlHelper.domNode2String(alphabet, true));
                }
            }
        }
    }

    @Test
    public void testImportTopTen_AlphabetAdd2NewClassification() throws Exception {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-Alphabet3.xml");
        List<Security> securities = new ArrayList<>();
        securities.addAll(createTestSecurityEtfLU1681043599Alphabet());
        securities.addAll(createTestSecurityEtfIE000CNSFAR2Alphabet());

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_TOPTEN)) {
                    Element alphabet = portfolioDocumentService.findClassificationByName(taxonomyElement, "Alphabet A (Google)");
                    assertNull(alphabet);

                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, securities, taxonomyElement);
                    assertEquals(4, importedTopTen.size());

                    alphabet = portfolioDocumentService.findClassificationByName(taxonomyElement, "Alphabet A (Google)");
                    List<Element> foundAssignments = portfolioDocumentService.findAssignmentsBySecurityIndex(alphabet, 1);
                    assertFalse(foundAssignments.isEmpty());
                    assertEquals(2, foundAssignments.size());
                    foundAssignments = portfolioDocumentService.findAssignmentsBySecurityIndex(alphabet, 2);
                    assertFalse(foundAssignments.isEmpty());
                    assertEquals(2, foundAssignments.size());
//                    logger.info(xmlHelper.domNode2String(alphabet, true));
                }
            }
        }
    }

    @Test
    public void testImportTopTen_AlphabetAdd2ExistingClassificationSecondHolding() throws Exception {
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-Alphabet4.xml");
        List<Security> securities = new ArrayList<>();
        securities.addAll(createTestSecurityEtfLU1681043599Alphabet());
        securities.addAll(createTestSecurityEtfIE000CNSFAR2Alphabet());
        securities.addAll(createTestSecurityEtfDE000A0F5UF5Alphabet());

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_TOPTEN)) {
                    Element alphabet = portfolioDocumentService.findClassificationByName(taxonomyElement, "Alphabet A (Google)");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(alphabet, 1);
                    assertNotNull(assignment);
                    assignment = portfolioDocumentService.findAssignmentBySecurityIndex(alphabet, 2);
                    assertNotNull(assignment);

                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, securities, securities, taxonomyElement);
                    assertEquals(4, importedTopTen.size());
//                    logger.info(xmlHelper.domNode2String(alphabet, true));

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

    @Test
    public void testImportTopTen_IE000CNSFAR2_RemoveOneEntryAndParentClassificationFolderDueToInactiveSecurity() throws Exception {
        // nothing to be removed by import
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-IE000CNSFAR2-Inactive-Security.xml");

        Security msciWorldEtf = new ETF("IE000CNSFAR2", 0, true);
        Map<String, Double> holdings = msciWorldEtf.getHoldings();
        holdings.put("Apple", 20d);
        holdings.put("Microsoft", 18d);

        Security steinhoffShare = new Share("NL0011375019", 1, false);
        steinhoffShare.setName("Steinhoff International Holdings N.V.");
        holdings = steinhoffShare.getHoldings();
        holdings.put("Steinhoff International Holdings N.V.", 100d);

        List<Security> allSecurities = new ArrayList<>(1);
        allSecurities.add(msciWorldEtf);
        allSecurities.add(steinhoffShare);
        List<Security> activeSecurities = new ArrayList<>(1);
        activeSecurities.add(msciWorldEtf);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_TOPTEN)) {
                    Element amd = portfolioDocumentService.findClassificationByName(taxonomyElement, "AMD");
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(amd, 1);
                    assertNotNull(assignment);

                    Element steinhoff = portfolioDocumentService.findClassificationByName(taxonomyElement, "Steinhoff International Holdings N.V.");
                    Element assignmentSteinhoff = portfolioDocumentService.findAssignmentBySecurityIndex(amd, 1);
                    assertNotNull(assignmentSteinhoff);

                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, allSecurities, activeSecurities, taxonomyElement);
                    assertEquals(2, importedTopTen.size());

                    amd = portfolioDocumentService.findClassificationByName(taxonomyElement, "AMD");
                    assertNull(amd);

                    steinhoff = portfolioDocumentService.findClassificationByName(taxonomyElement, "Steinhoff International Holdings N.V.");
                    assertNull(steinhoff);
                }
            }
        }
//        XmlFileWriter xmlFileWriter = new XmlFileWriter();
//        xmlFileWriter.writeXml(portfolioDocument, BASE_TEST_PATH + "classification-topten-IE000CNSFAR2-Inactive-Security-RESULT.xml");
    }

    @Test
    public void testImportTopTen_MetaPlatforms_DoNotChangeNameOfExistingGrouping() throws Exception {
        // there is already a group called "Meta Platforms Inc." and this should not change by re-calculating the top 10
        final String CLASSIFICATION_META = "Meta Platforms Inc.";
        Document portfolioDocument = xmlHelper.readXmlStream(BASE_TEST_PATH + "classification-topten-Meta.xml");

        Security msciWorldEtfT = new ETF("LU1681043599", 1, true);
        Map<String, Double> holdings = msciWorldEtfT.getHoldings();
        holdings.put("Meta Platforms Inc", 1d);

        Security msciWorldEtfD = new ETF("IE000CNSFAR2", 2, true);
        holdings = msciWorldEtfD.getHoldings();
        holdings.put("Meta Platforms (ehem. Facebook)", 2d);

        Security msciUSA2x = new ETF("FR0010755611", 3, true);
        holdings = msciUSA2x.getHoldings();
        holdings.put("Meta Platforms Inc.", 3d);

        Security nasdaq100 = new ETF("DE000A0F5UF5", 4, true);
        holdings = nasdaq100.getHoldings();
        holdings.put("Meta Platforms Inc.", 4d);

        Security ftseNorthAmerica = new ETF("IE00BKX55R35", 5, true);
        holdings = ftseNorthAmerica.getHoldings();
        holdings.put("Meta Platforms Inc.", 5d);

        Security stoxxGlobalSelectDividend = new ETF("LU0292096186", 6, true);

        List<Security> allSecurities = new ArrayList<>(5);
        allSecurities.add(msciWorldEtfD);
        allSecurities.add(msciWorldEtfT);
        allSecurities.add(msciUSA2x);
        allSecurities.add(nasdaq100);
        allSecurities.add(ftseNorthAmerica);
        allSecurities.add(stoxxGlobalSelectDividend);
        List<Security> activeSecurities = new ArrayList<>(5);
        activeSecurities.add(msciWorldEtfD);
        activeSecurities.add(msciWorldEtfT);
        activeSecurities.add(msciUSA2x);
        activeSecurities.add(nasdaq100);
        activeSecurities.add(ftseNorthAmerica);
        activeSecurities.add(stoxxGlobalSelectDividend);

        NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
        for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
            Node taxonomyNode = listOfTaxonomies.item(i);
            if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element taxonomyElement = (Element) taxonomyNode;
                String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                if (taxonomyName.equals(TAXONOMY_TOPTEN)) {
                    Element meta = portfolioDocumentService.findClassificationByName(taxonomyElement, CLASSIFICATION_META);
                    Element assignment = portfolioDocumentService.findAssignmentBySecurityIndex(meta, 1);
                    assertNotNull(assignment);

                    JsonArray importedTopTen = portfolioDocumentService.importCompanyRatio(portfolioDocument, allSecurities, activeSecurities, taxonomyElement);
                    assertEquals(3, importedTopTen.size());

                    meta = portfolioDocumentService.findClassificationByName(taxonomyElement, CLASSIFICATION_META);
                    assertNotNull(meta);

                    NodeList assignments = taxonomyElement.getElementsByTagName("assignment");
                    assertNotNull(assignments);
                    assertEquals(5, assignments.getLength());
                }
            }
        }
        XmlFileWriter xmlFileWriter = new XmlFileWriter();
        xmlFileWriter.writeXml(portfolioDocument, BASE_TEST_PATH + "classification-topten-Meta-RESULT.xml");
    }

}
