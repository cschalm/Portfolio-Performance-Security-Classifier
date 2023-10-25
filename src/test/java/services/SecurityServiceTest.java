package services;

import enums.SecurityType;
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
import java.util.ArrayList;
import java.util.List;

import static enums.SecurityType.ETF;
import static enums.SecurityType.FOND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SecurityServiceTest extends AbstractTest {
    SecurityService service = new SecurityService();
    XmlHelper xmlHelper = new XmlHelper();

    @Test
    public void processSecurities() throws IOException, ParserConfigurationException, SAXException {
        Document document = xmlHelper.readXmlStream(Files.newInputStream(new File(BASE_TEST_PATH + "EtfSecurity.xml").toPath()));
        NodeList securityNodes = document.getElementsByTagName("security");
        assertNotNull(securityNodes);
        assertEquals(1, securityNodes.getLength());

        List<Security> securityList = service.processSecurities(securityNodes);
        assertNotNull(securityList);
        assertEquals(1, securityList.size());
    }
}