import enums.SecurityType;
import models.Security;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import services.SecurityService;
import xml.XmlFileWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static constants.PathConstants.BASE_PATH;
import static constants.PathConstants.FILE_NAME;
import static xml.XmlFileReader.getAllSecurities;
import static xml.XmlFileReader.getDocument;

public class Main {
    SecurityService securityService = new SecurityService();
    XmlFileWriter xmlFileWriter = new XmlFileWriter();

    public static void main(String[] args) {
        Main main = new Main();
        main.run();
    }

    private void run() {
        System.out.print("----- Start -----\n");
        System.out.println("Working Directory = " + BASE_PATH);

        Document portfolioDocument = loadPortfolioDocumentFromFile();

        NodeList allSecurities = getAllSecuritiesFromPortfolio(portfolioDocument);

        List<SecurityType> requestedSecurityTypes = inputSecurities();

        Security[] updatedSecurities = addClassificationData(allSecurities, requestedSecurityTypes);

        xmlFileWriter.updateXml(portfolioDocument, updatedSecurities);

        System.out.print("----- END -----\n");
    }

    Security[] addClassificationData(NodeList allSecurities, List<SecurityType> requestedSecurityTypes) {
        return securityService.processSecurities(allSecurities, requestedSecurityTypes);
    }

    List<SecurityType> inputSecurities() {
        System.out.print("\n\nChoose security types:\n- ETF\n- Fond\n- all\n");
        Scanner userInput = new Scanner(System.in);
        String input = userInput.nextLine().toLowerCase();

        List<SecurityType> inputSecurities = new ArrayList<>();

        if (input.equals("etf")) {
            inputSecurities.add(SecurityType.ETF);
        }
        if (input.equals("fond")) {
            inputSecurities.add(SecurityType.FOND);
        }
        if (input.equals("all")) {
            inputSecurities.add(SecurityType.ETF);
            inputSecurities.add(SecurityType.FOND);
        }

        return inputSecurities;
    }

    NodeList getAllSecuritiesFromPortfolio(Document portfolioDoc) {
        return getAllSecurities(portfolioDoc);
    }

    Document loadPortfolioDocumentFromFile() {
        return getDocument(BASE_PATH + FILE_NAME);
    }

}