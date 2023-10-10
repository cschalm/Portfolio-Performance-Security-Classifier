import enums.SecurityType;
import models.Security;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import services.SecurityService;
import xml.XmlFileWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static constants.PathConstants.BASE_PATH;
import static constants.PathConstants.FILE_NAME;
import static xml.XmlFileReader.getAllSecurities;
import static xml.XmlFileReader.getDocument;

public class Main {

    public static void main(String[] args) {
        Main main = new Main();
        main.run();
    }

    private void run() {
        System.out.printf("----- Start -----\n");
        System.out.println("Working Directory = " + BASE_PATH);

        Document doc = getDocument(BASE_PATH + FILE_NAME);
        NodeList allSecurities = getAllSecurities(doc);

        List<SecurityType> requestedSecurityTypes = inputSecurities();

        SecurityService securityService = new SecurityService();
        Security[] allSecuritiesProcessed = securityService.processSecurities(allSecurities, requestedSecurityTypes);
        List<Security> allSecurities2 = new ArrayList<>(Arrays.asList(allSecuritiesProcessed));

        XmlFileWriter xmlFileWriter = new XmlFileWriter();
        xmlFileWriter.updateXml(doc, allSecuritiesProcessed);

        System.out.printf("----- END -----\n");

    }

    private List<SecurityType> inputSecurities() {
        System.out.printf("\n\nChoose security types:\n- ETF\n- Fond\n- all\n");
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

}