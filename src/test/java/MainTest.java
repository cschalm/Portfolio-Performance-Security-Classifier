import enums.SecurityType;
import models.Security;
import org.junit.Test;
import org.schalm.test.AbstractTest;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import xml.XmlHelper;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static enums.SecurityType.ETF;
import static enums.SecurityType.FOND;
import static org.junit.Assert.*;

public class MainTest extends AbstractTest {
    private Main main = new Main();
    XmlHelper xmlHelper = new XmlHelper();

    @Test
    public void addClassificationData() throws IOException, ParserConfigurationException, SAXException {
        Document document = xmlHelper.readXmlStream(Files.newInputStream(new File("src/test/resources/EtfSecurity.xml").toPath()));
        NodeList securityNodes = document.getElementsByTagName("security");
        assertNotNull(securityNodes);
        assertEquals(1, securityNodes.getLength());

        List<Security> securities = main.addClassificationData(securityNodes);
        assertEquals(1, securities.size());
    }

}