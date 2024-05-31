import models.Security;
import models.SecurityDetailsCache;
import org.apache.commons.cli.*;
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
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static constants.PathConstants.CACHE_FILE_NAME;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getCanonicalName());
    SecurityService securityService = new SecurityService();
    PortfolioDocumentService portfolioDocumentService = new PortfolioDocumentService();
    XmlFileWriter xmlFileWriter = new XmlFileWriter();
    XmlHelper xmlHelper = new XmlHelper();

    public static void main(String[] args) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        Main main = new Main();

        Options options = new Options();
        Option help = new Option("help", "print this message");
        options.addOption(help);
        Option inputFile = Option.builder("inputfile")
                .argName("file")
                .hasArg()
                .desc("source Portfolio Performance xml-file")
                .build();
        options.addOption(inputFile);
        Option outputFile = Option.builder("outputfile")
                .argName("file")
                .hasArg()
                .desc("destination Portfolio Performance xml-file (will be overridden!!!)")
                .build();
        options.addOption(outputFile);
        Option cacheDir = Option.builder("cachedir")
                .argName("directory")
                .hasArg()
                .desc("directory (re-) used for caching information for each security")
                .build();
        options.addOption(cacheDir);
        Option logConfigFile = Option.builder("logconfig")
                .argName("file")
                .hasArg()
                .desc("java.util.logging logging.properties")
                .build();
        options.addOption(logConfigFile);

        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (line.hasOption(help)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("PortfolioPerformanceSecurityClassifier", options);
            } else {
                {
                    InputStream logConfig;
                    String location = "default logging.properties";
                    if (line.hasOption(logConfigFile)) {
                        location = line.getOptionValue(logConfigFile);
                        logConfig = main.getClass().getResourceAsStream(location);
                    } else {
                        logConfig = main.getClass().getResourceAsStream("logging.properties");
                    }
                    LogManager logManager = LogManager.getLogManager();
                    logManager.readConfiguration(logConfig);
                    logger.info("logConfig: " + location);
                }
                String cacheDirFile;
                {
                    if (line.hasOption(cacheDir)) {
                        cacheDirFile = line.getOptionValue(cacheDir);
                    } else {
                        cacheDirFile = Files.createTempDirectory("-cache").toFile().getAbsolutePath();
                    }
                }
                String inputFileName = line.getOptionValue(inputFile);
                if (inputFileName == null) {
                    System.err.println("Missing required parameter " + inputFile.getArgName());
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp("PortfolioPerformanceSecurityClassifier", options);
                }
                String outputFileName = line.getOptionValue(outputFile);
                if (outputFileName == null) {
                    System.err.println("Missing required parameter " + outputFile.getArgName());
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp("PortfolioPerformanceSecurityClassifier", options);
                }
                main.run(inputFileName, outputFileName, cacheDirFile);
            }
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing of parameters failed. Reason: " + exp.getMessage());
        }
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