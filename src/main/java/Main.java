import models.Security;
import models.SecurityDetailsCache;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import services.PortfolioDocumentService;
import services.SecurityService;
import xml.XmlFileWriter;
import xml.XmlHelper;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static constants.PathConstants.*;
import static xml.XmlFileReader.getAllSecurities;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getCanonicalName());
    SecurityService securityService = new SecurityService();
    PortfolioDocumentService portfolioDocumentService = new PortfolioDocumentService();
    XmlFileWriter xmlFileWriter = new XmlFileWriter();
    XmlHelper xmlHelper = new XmlHelper();

    public static void main(String[] args) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        Main main = new Main();
        main.run();
    }

    private void run() throws IOException, TransformerException, ParserConfigurationException, SAXException {
        LogManager logManager = LogManager.getLogManager();
        logManager.readConfiguration(new FileInputStream("src/main/resources/logging.properties"));
        logger.info("Working Directory = " + BASE_PATH);

        Document portfolioDocument = loadPortfolioDocumentFromFile();

        NodeList allSecurities = getAllSecuritiesFromPortfolio(portfolioDocument);
        List<Security> updatedSecurities = addClassificationData(allSecurities);

        {
            // remove cache-file to force analysis
            File cacheFile = new File(SAVE_FILE);
            if (cacheFile.exists())
                logger.info("Cache-file deleted: " + cacheFile.delete());
        }
        SecurityDetailsCache securityDetailsCache = new SecurityDetailsCache(SAVE_FILE);
        portfolioDocumentService.updateXml(portfolioDocument, updatedSecurities, securityDetailsCache);

        xmlFileWriter.writeXml(portfolioDocument, BASE_PATH + OUTPUT_FILE_NAME);
    }

    List<Security> addClassificationData(NodeList allSecurities) {
        return securityService.processSecurities(allSecurities);
    }

    NodeList getAllSecuritiesFromPortfolio(Document portfolioDoc) {
        return getAllSecurities(portfolioDoc);
    }

    Document loadPortfolioDocumentFromFile() throws IOException, ParserConfigurationException, SAXException {
        return xmlHelper.readXmlStream(BASE_PATH + INPUT_FILE_NAME);
    }

}