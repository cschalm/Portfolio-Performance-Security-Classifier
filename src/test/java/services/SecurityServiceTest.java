package services;

import models.Security;
import org.junit.Test;
import org.schalm.test.AbstractTest;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import xml.XmlHelper;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class SecurityServiceTest extends AbstractTest {
    XmlHelper xmlHelper = new XmlHelper();

    @Test
    public void processSecurities() throws IOException, ParserConfigurationException, SAXException {
        SecurityService service = new SecurityService();
        Document document = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "EtfSecurity.xml").toPath()));
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
        Security security = service.createSecurity("IE00BYYHSM20");
        assertNotNull(security);
        assertEquals("IE00BYYHSM20", security.getIsin());
        assertNotNull(security.getBranches());
        assertFalse(security.getBranches().isEmpty());
        assertNotNull(security.getCountries());
        assertFalse(security.getCountries().isEmpty());
        assertNotNull(security.getCurrencies());
        assertFalse(security.getCurrencies().isEmpty());
        assertNotNull(security.getHoldings());
        assertFalse(security.getHoldings().isEmpty());
    }

    @Test
    public void createSecurityStock() {
        SecurityService service = new SecurityService(BASE_TEST_PATH + "cache/");
        Security security = service.createSecurity("DE000TUAG505");
        assertNotNull(security);
        assertEquals("DE000TUAG505", security.getIsin());
        assertNotNull(security.getBranches());
        assertFalse(security.getBranches().isEmpty());
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
}