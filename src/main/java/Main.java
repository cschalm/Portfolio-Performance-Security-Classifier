import models.Security;
import models.SecurityDetailsCache;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import services.PortfolioDocumentService;
import services.SecurityService;
import xml.XmlFileReader;
import xml.XmlFileWriter;
import xml.XmlHelper;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static constants.PathConstants.*;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getCanonicalName());
    SecurityService securityService = new SecurityService();
    PortfolioDocumentService portfolioDocumentService = new PortfolioDocumentService();
    XmlFileWriter xmlFileWriter = new XmlFileWriter();
    XmlHelper xmlHelper = new XmlHelper();

    public static void main(String[] args) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        Main main = new Main();
        main.initializeLogging();
        String inputFileName = Optional.ofNullable(System.getProperty("input")).orElse(BASE_PATH + INPUT_FILE_NAME);
        String outputFileName = Optional.ofNullable(System.getProperty("output")).orElse(BASE_PATH + OUTPUT_FILE_NAME);
        String cacheDir = main.initializeCacheDir();
        main.run(inputFileName, outputFileName, cacheDir);
    }

    private String initializeCacheDir() throws IOException {
        Optional<String> cacheDirName = Optional.ofNullable(System.getProperty("cachedir"));
        String cacheDir;
        if (cacheDirName.isPresent()) {
            cacheDir = cacheDirName.get();
        } else {
            cacheDir = Files.createTempDirectory("-cache").toFile().getAbsolutePath();
        }

        return cacheDir;
    }

    private void initializeLogging() throws IOException {
        Optional<String> logConfigFileName = Optional.ofNullable(System.getProperty("logconfig"));
        InputStream logConfig;
        String location = "default logging.properties";
        if (logConfigFileName.isPresent()) {
            logConfig = this.getClass().getResourceAsStream(logConfigFileName.get());
            location = logConfigFileName.get();
        } else {
            logConfig = this.getClass().getResourceAsStream("logging.properties");
        }
        LogManager logManager = LogManager.getLogManager();
        logManager.readConfiguration(logConfig);

        logger.info("logConfig: " + location);
    }

    private void run(String inputFileName, String outputFileName, String cacheDir) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        logger.info("inputFileName = " + inputFileName);
        logger.info("outputFileName = " + outputFileName);
        logger.info("cacheDir = " + cacheDir);

        securityService = new SecurityService(cacheDir);
        SecurityDetailsCache securityDetailsCache = new SecurityDetailsCache(cacheDir + FileSystems.getDefault().getSeparator() + CACHE_FILE_NAME);

        Document portfolioDocument = loadPortfolioDocumentFromFile(inputFileName);

        NodeList allSecurities = getAllSecuritiesFromPortfolio(portfolioDocument);
        List<Security> updatedSecurities = addClassificationData(allSecurities);

        portfolioDocumentService.updateXml(portfolioDocument, updatedSecurities, securityDetailsCache);

        xmlFileWriter.writeXml(portfolioDocument, outputFileName);
    }

    List<Security> addClassificationData(NodeList allSecurities) {
        return securityService.processSecurities(allSecurities);
    }

    NodeList getAllSecuritiesFromPortfolio(Document portfolioDoc) {
        return new XmlFileReader().getAllSecurities(portfolioDoc);
    }

    Document loadPortfolioDocumentFromFile(String inputFileName) throws IOException, ParserConfigurationException, SAXException {
        return xmlHelper.readXmlStream(inputFileName);
    }

}