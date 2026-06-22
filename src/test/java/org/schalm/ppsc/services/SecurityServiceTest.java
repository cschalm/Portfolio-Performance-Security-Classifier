package org.schalm.ppsc.services;

import org.junit.Test;
import org.schalm.ppsc.models.Commodity;
import org.schalm.ppsc.models.ETF;
import org.schalm.ppsc.models.Security;
import org.schalm.ppsc.models.Share;
import org.schalm.ppsc.xml.XmlHelper;
import org.schalm.test.AbstractTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;
import static org.schalm.ppsc.constants.PathConstants.BASE_TARGET_PATH;

public class SecurityServiceTest extends AbstractTest {
    XmlHelper xmlHelper = new XmlHelper();

    @Test
    public void processSecurities() throws IOException, ParserConfigurationException, SAXException {
        SecurityService service = new SecurityService();
        Document document = xmlHelper.readXmlStream(BASE_TEST_PATH + "EtfSecurity.xml");
        NodeList securityNodes = document.getElementsByTagName("security");
        assertNotNull(securityNodes);
        assertEquals(1, securityNodes.getLength());

        List<Security> securityList = service.processSecurities(securityNodes);
        assertNotNull(securityList);
        assertEquals(1, securityList.size());
    }

    @Test
    public void processSecuritiesRetired() throws IOException, ParserConfigurationException, SAXException {
        SecurityService service = new SecurityService();
        Document document = xmlHelper.readXmlStream(BASE_TEST_PATH + "Security-retired.xml");
        NodeList securityNodes = document.getElementsByTagName("security");
        assertNotNull(securityNodes);
        assertEquals(1, securityNodes.getLength());

        List<Security> securityList = service.processSecurities(securityNodes);
        assertNotNull(securityList);
        assertEquals(1, securityList.size());
    }

    @Test
    public void processSecuritiesMixedRetiredAndActive() throws IOException, ParserConfigurationException, SAXException {
        SecurityService service = new SecurityService();
        Document document = xmlHelper.readXmlStream(BASE_TEST_PATH + "SecurityActiveInactive.xml");
        NodeList securityNodes = document.getElementsByTagName("security");
        assertNotNull(securityNodes);
        assertEquals(2, securityNodes.getLength());

        List<Security> securityList = service.processSecurities(securityNodes);
        assertNotNull(securityList);
        assertEquals(2, securityList.size());
        assertEquals(1, securityList.stream().filter(Security::isActive).count());
        assertEquals(1, securityList.stream().filter(security -> !security.isActive()).count());
    }

    @Test
    public void createSecurityEtf() {
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE00BYYHSM20", 0, true);
        assertNotNull(security);
        assertEquals(ETF.class, security.getClass());
        assertEquals("IE00BYYHSM20", security.getIsin());
        assertTrue(security.isActive());
        assertTrue(security.isETF());
        assertFalse(security.isCommodity());
        assertFalse(security.isFund());
        assertFalse(security.isShare());
        assertNotNull(security.getIndustries());
        assertFalse(security.getIndustries().isEmpty());
        assertNotNull(security.getCountries());
        assertFalse(security.getCountries().isEmpty());
        assertNotNull(security.getHoldings());
        assertFalse(security.getHoldings().isEmpty());
    }

    @Test
    public void createSecurityShare() {
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("DE000TUAG505", 0, true);
        assertNotNull(security);
        assertEquals(Share.class, security.getClass());
        assertEquals("DE000TUAG505", security.getIsin());
        assertTrue(security.isActive());
        assertTrue(security.isShare());
        assertFalse(security.isCommodity());
        assertFalse(security.isFund());
        assertFalse(security.isETF());
        assertNotNull(security.getIndustries());
        assertFalse(security.getIndustries().isEmpty());
        assertNotNull(security.getCountries());
        assertFalse(security.getCountries().isEmpty());
        assertNotNull(security.getHoldings());
        assertFalse(security.getHoldings().isEmpty());
    }

    @Test
    public void createSecurityCommodityGold() {
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("XC0009655157", 0, true);
        assertNotNull(security);
        assertEquals(Commodity.class, security.getClass());
        assertEquals("XC0009655157", security.getIsin());
        assertTrue(security.isActive());
        assertTrue(security.isCommodity());
        assertFalse(security.isETF());
        assertFalse(security.isFund());
        assertFalse(security.isShare());
        assertNotNull(security.getIndustries());
        assertTrue(security.getIndustries().isEmpty());
        assertNotNull(security.getCountries());
        assertTrue(security.getCountries().isEmpty());
        assertNotNull(security.getHoldings());
        assertTrue(security.getHoldings().isEmpty());
    }

    @Test
    public void getMappedPercentageForNode() {
    }

    @Test
    public void getHoldingPercentageMap() {
    }

    @Test
    public void createSecurityEtfMsciWorld() {
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE000CNSFAR2", 0, true);
        assertNotNull(security);
        assertEquals(ETF.class, security.getClass());
        assertEquals("IE000CNSFAR2", security.getIsin());
        assertTrue(security.isActive());
        assertTrue(security.isETF());
        assertFalse(security.isCommodity());
        assertFalse(security.isFund());
        assertFalse(security.isShare());
        assertNotNull(security.getIndustries());
        assertFalse(security.getIndustries().isEmpty());
        assertNotNull(security.getCountries());
        assertFalse(security.getCountries().isEmpty());
        assertNotNull(security.getHoldings());
        assertFalse(security.getHoldings().isEmpty());
    }

    @Test
    public void createSecurityShareAres() {
        SecurityService service = new SecurityService(BASE_TARGET_PATH + "cache/");
        Security security = service.createSecurity("US04010L1035", 0, true);
        assertNotNull(security);
        assertEquals(Share.class, security.getClass());
        assertEquals("US04010L1035", security.getIsin());
        assertTrue(security.isActive());
        assertTrue(security.isShare());
        assertFalse(security.isCommodity());
        assertFalse(security.isFund());
        assertFalse(security.isETF());
        assertNotNull(security.getIndustries());
        assertFalse(security.getIndustries().isEmpty());
        assertNotNull(security.getCountries());
        assertFalse(security.getCountries().isEmpty());
        assertNotNull(security.getHoldings());
        assertFalse(security.getHoldings().isEmpty());
    }

    @Test
    public void createSecurityShareInactive() {
        SecurityService service = new SecurityService(BASE_TARGET_PATH + "cache/");
        Security security = service.createSecurity("US04010L1035", 0, false);
        assertNotNull(security);
        assertEquals(Share.class, security.getClass());
        assertEquals("US04010L1035", security.getIsin());
        assertFalse(security.isActive());
        assertTrue(security.isShare());
        assertFalse(security.isCommodity());
        assertFalse(security.isFund());
        assertFalse(security.isETF());
        assertNotNull(security.getIndustries());
        assertFalse(security.getIndustries().isEmpty());
        assertNotNull(security.getCountries());
        assertFalse(security.getCountries().isEmpty());
        assertNotNull(security.getHoldings());
        assertFalse(security.getHoldings().isEmpty());
    }

    @Test
    public void removeOldPricesRetired() throws IOException, ParserConfigurationException, SAXException {
        SecurityService service = new SecurityService();
        Document document = xmlHelper.readXmlStream(BASE_TEST_PATH + "Security-retired.xml");
        NodeList securityNodes = document.getElementsByTagName("security");
        assertNotNull(securityNodes);
        assertEquals(1, securityNodes.getLength());
        Element securitiesElement = (Element) securityNodes.item(0);
        int noOfPrices = securitiesElement.getElementsByTagName("price").getLength();
        assertEquals(1111, noOfPrices);

        int removedCount = service.removeOldPrices(securitiesElement, LocalDate.parse("2025-01-01"));
        assertEquals(1111, removedCount);
        assertNotNull(securityNodes);
        assertEquals(1, securityNodes.getLength());
        noOfPrices = securitiesElement.getElementsByTagName("price").getLength();
        assertEquals(0, noOfPrices);
    }

    @Test
    public void removeOldPricesActive() throws IOException, ParserConfigurationException, SAXException {
        SecurityService service = new SecurityService();
        Document document = xmlHelper.readXmlStream(BASE_TEST_PATH + "Security-oldPrices.xml");
        NodeList securityNodes = document.getElementsByTagName("security");
        assertNotNull(securityNodes);
        assertEquals(1, securityNodes.getLength());
        Element securitiesElement = (Element) securityNodes.item(0);
        int noOfPrices = securitiesElement.getElementsByTagName("price").getLength();
        assertEquals(2404, noOfPrices);

        int removedCount = service.removeOldPrices(securitiesElement, LocalDate.parse("2023-01-01"));
        assertEquals(2048, removedCount);
        assertNotNull(securityNodes);
        assertEquals(1, securityNodes.getLength());
        noOfPrices = securitiesElement.getElementsByTagName("price").getLength();
        assertEquals(356, noOfPrices);
    }

    @Test
    public void removeOldPricesCommodity() throws IOException, ParserConfigurationException, SAXException {
        SecurityService service = new SecurityService();
        Document document = xmlHelper.readXmlStream(BASE_TEST_PATH + "Security-commodity.xml");
        NodeList securityNodes = document.getElementsByTagName("security");
        assertNotNull(securityNodes);
        assertEquals(1, securityNodes.getLength());
        Element securitiesElement = (Element) securityNodes.item(0);
        int noOfPrices = securitiesElement.getElementsByTagName("price").getLength();
        assertEquals(711, noOfPrices);

        int removedCount = service.removeOldPrices(securitiesElement, LocalDate.parse("2026-01-01"));
        assertEquals(692, removedCount);
        assertNotNull(securityNodes);
        assertEquals(1, securityNodes.getLength());
        noOfPrices = securitiesElement.getElementsByTagName("price").getLength();
        assertEquals(19, noOfPrices);
    }

    @Test
    public void createSecurityShareInactiveSteinhoffInternational() {
        SecurityService service = new SecurityService(BASE_TARGET_PATH + "cache/");
        Security security = service.createSecurity("NL0011375019", 0, false);
        assertNotNull(security);
        assertEquals(Share.class, security.getClass());
        assertEquals("NL0011375019", security.getIsin());
        assertFalse(security.isActive());
        assertTrue(security.isShare());
        assertFalse(security.isCommodity());
        assertFalse(security.isFund());
        assertFalse(security.isETF());
        assertNotNull(security.getIndustries());
        assertFalse(security.getIndustries().isEmpty());
        assertNotNull(security.getCountries());
        assertFalse(security.getCountries().isEmpty());
        assertNotNull(security.getHoldings());
        assertFalse(security.getHoldings().isEmpty());
    }

}
