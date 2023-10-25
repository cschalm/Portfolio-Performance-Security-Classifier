import models.Security;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import services.SecurityService;
import xml.XmlFileWriter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static constants.PathConstants.BASE_PATH;
import static constants.PathConstants.INPUT_FILE_NAME;
import static xml.XmlFileReader.getAllSecurities;
import static xml.XmlFileReader.getDocument;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getCanonicalName());
    SecurityService securityService = new SecurityService();
    XmlFileWriter xmlFileWriter = new XmlFileWriter();

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.run();
    }

    private void run() throws IOException {
        LogManager logManager = LogManager.getLogManager();
        logManager.readConfiguration(new FileInputStream("src/main/resources/logging.properties"));
        logger.info("----- Start -----");
        logger.info("Working Directory = " + BASE_PATH);

        Document portfolioDocument = loadPortfolioDocumentFromFile();

        NodeList allSecurities = getAllSecuritiesFromPortfolio(portfolioDocument);

        List<Security> updatedSecurities = addClassificationData(allSecurities);

        xmlFileWriter.updateXml(portfolioDocument, updatedSecurities.toArray(new Security[0]), BASE_PATH);

        logger.info("----- END -----\n");
    }

    List<Security> addClassificationData(NodeList allSecurities) {
        return securityService.processSecurities(allSecurities);
    }

    NodeList getAllSecuritiesFromPortfolio(Document portfolioDoc) {
        return getAllSecurities(portfolioDoc);
    }

    Document loadPortfolioDocumentFromFile() {
        return getDocument(BASE_PATH + INPUT_FILE_NAME);
    }

}