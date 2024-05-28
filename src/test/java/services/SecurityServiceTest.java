package services;

import models.Security;
import org.junit.Test;
import org.schalm.test.AbstractTest;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import xml.XmlHelper;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

import static constants.PathConstants.BASE_TARGET_PATH;
import static org.junit.Assert.*;

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
    public void createSecurityEtf() {
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("IE00BYYHSM20", 0);
        assertNotNull(security);
        assertEquals("IE00BYYHSM20", security.getIsin());
        assertNotNull(security.getIndustries());
        assertFalse(security.getIndustries().isEmpty());
        assertNotNull(security.getCountries());
        assertFalse(security.getCountries().isEmpty());
        assertNotNull(security.getHoldings());
        assertFalse(security.getHoldings().isEmpty());
    }

    @Test
    public void createSecurityStock() {
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("DE000TUAG505", 0);
        assertNotNull(security);
        assertEquals("DE000TUAG505", security.getIsin());
        assertNotNull(security.getIndustries());
        assertFalse(security.getIndustries().isEmpty());
        assertNotNull(security.getCountries());
        assertFalse(security.getCountries().isEmpty());
        assertNotNull(security.getHoldings());
        assertFalse(security.getHoldings().isEmpty());
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
        Security security = service.createSecurity("IE000CNSFAR2", 0);
        assertNotNull(security);
        assertEquals("IE000CNSFAR2", security.getIsin());
        assertNotNull(security.getIndustries());
        assertFalse(security.getIndustries().isEmpty());
        assertNotNull(security.getCountries());
        assertFalse(security.getCountries().isEmpty());
        assertNotNull(security.getHoldings());
        assertFalse(security.getHoldings().isEmpty());
    }

    @Test
    public void createSecurityStockAres() {
        SecurityService service = new SecurityService(BASE_TARGET_PATH + "cache/");
        Security security = service.createSecurity("US04010L1035", 0);
        assertNotNull(security);
        assertEquals("US04010L1035", security.getIsin());
        assertNotNull(security.getIndustries());
        assertFalse(security.getIndustries().isEmpty());
        assertNotNull(security.getCountries());
        assertFalse(security.getCountries().isEmpty());
        assertNotNull(security.getHoldings());
        assertFalse(security.getHoldings().isEmpty());
    }

}