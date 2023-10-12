package services;

import enums.SecurityType;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import xml.XmlHelper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static enums.SecurityType.ETF;
import static enums.SecurityType.FOND;
import static org.junit.Assert.*;

public class SecurityServiceTest {
    SecurityService service = new SecurityService();
    XmlHelper xmlHelper = new XmlHelper();


    @Test
    public void processSecurities() throws IOException, ParserConfigurationException, SAXException {
        Document document = xmlHelper.readXmlStream(Files.newInputStream(new File("src/test/resources/EtfSecurity.xml").toPath()));
        NodeList securityNodes = document.getElementsByTagName("security");
        assertNotNull(securityNodes);
        assertEquals(1, securityNodes.getLength());
        List<SecurityType> securityTypes = new ArrayList<>();
        securityTypes.add(ETF);
        securityTypes.add(FOND);

        service.processSecurities(securityNodes, securityTypes);
    }
}