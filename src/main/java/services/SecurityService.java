package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import enums.SecurityType;
import models.Security;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xml.XmlHelper;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SecurityService {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    XmlHelper xmlHelper = new XmlHelper();

    public Security[] processSecurities(NodeList oAllSecurities, List<SecurityType> requestedSecurityTypes) {
        Security[] allSecurity = new Security[oAllSecurities.getLength()];
        //System.out.printf("oAllSecurities.getLength(): %s\n", oAllSecurities.getLength());
        for (int i = 0; i < oAllSecurities.getLength(); i++) {
            processSecurity(oAllSecurities, requestedSecurityTypes, i, allSecurity);
        }

        return allSecurity;
    }

    void processSecurity(NodeList oAllSecurities, List<SecurityType> requestedSecurityTypes, int i, Security[] allSecurity) {
        Element securitiesElement = (Element) oAllSecurities.item(i);
        String isin = xmlHelper.getTextContent(securitiesElement, "isin");
        String isRetired = xmlHelper.getTextContent(securitiesElement, "isRetired");

        if (!isin.isEmpty() && "false".equals(isRetired)) {
            System.out.print("Fetching data for \"" + isin + "\"... - ");
            allSecurity[i] = createSecurity(isin, requestedSecurityTypes);
            String name = xmlHelper.getTextContent(securitiesElement, "name");
            if (!name.isEmpty()) {
                allSecurity[i].setName(name);
            }
            System.out.print(" done!\n");
        }
    }


    boolean isETF(String strResponseFromFirstCall, List<SecurityType> allowedSecurities) {
        return strResponseFromFirstCall.startsWith("/etf/anlageschwerpunkt")
                && allowedSecurities.contains(SecurityType.ETF);
    }

    boolean isFond(String strResponseFromFirstCall, List<SecurityType> allowedSecurities) {
        return strResponseFromFirstCall.startsWith("/fonds/anlageschwerpunkt")
                && allowedSecurities.contains(SecurityType.FOND);
    }

    Security createSecurity(String strIsin, List<SecurityType> requestedSecurityTypes) {
        Security security = new Security(strIsin);
        boolean debug = !true;
        try {
            String pathForIsin = readStringFromURL("https://www.onvista.de/etf/anlageschwerpunkt/" + strIsin);
            String name = pathForIsin.split("/")[2];
            System.out.print(pathForIsin + " (" + name + ")");
            security.setName(name);

            // Check if ETF or fond
            boolean isEftOrFond = isETF(pathForIsin, requestedSecurityTypes)
                    || isFond(pathForIsin, requestedSecurityTypes);
            System.out.println(" - is ETF or Fond: " + isEftOrFond);
            if (isEftOrFond) {

                String htmlPageAnlageschwerpunkt = readStringFromURL("https://www.onvista.de" + pathForIsin);

                String[] splitResponse = htmlPageAnlageschwerpunkt.split("<script id=\"__NEXT_DATA__\" type=\"application/json\">");
                String jsonResponsePart = splitResponse[1].split("</script>")[0];

                JsonObject rootObj = JsonParser.parseString(jsonResponsePart).getAsJsonObject();
                JsonObject breakdownsNode = rootObj.getAsJsonObject("props").getAsJsonObject("pageProps").getAsJsonObject("data").getAsJsonObject("breakdowns");

                // parsing holdings
                if (breakdownsNode != null) {
                    JsonObject tempObjectHoldings = breakdownsNode.getAsJsonObject("fundsHoldingList");
                    JsonArray oArrayHoldingList = tempObjectHoldings != null ? tempObjectHoldings.getAsJsonArray("list") : new JsonArray();
                    Map<String, Security.PercentageUsedTuple> oListForHoldings = new HashMap<>();

                    for (int i = 0; i < oArrayHoldingList.size(); i++) {
                        JsonObject oHolding = ((JsonObject) oArrayHoldingList.get(i));
                        String strHoldingName = oHolding.getAsJsonObject("instrument").get("name").getAsString();
                        Double nHoldingPercent = oHolding.get("investmentPct").getAsDouble();
                        if (debug)
                            System.out.printf("Holding: %s; Percentage: %s%%\n", strHoldingName, DECIMAL_FORMAT.format(nHoldingPercent));
                        oListForHoldings.put(strHoldingName, security.new PercentageUsedTuple(nHoldingPercent, false));
                    }
                    security.setHoldings(oListForHoldings);

                    // parsing branches
                    security.setBranches(getListForNode(breakdownsNode.getAsJsonObject("branchBreakdown"), debug));

                    // parsing currency
                    security.setCurrencies(getListForNode(breakdownsNode.getAsJsonObject("currencyBreakdown"), debug));

                    // parsing instrument
                    security.setInstruments(getListForNode(breakdownsNode.getAsJsonObject("instrumentBreakdown"), debug));

                    // parsing country
                    security.setCountries(getListForNode(breakdownsNode.getAsJsonObject("countryBreakdown"), debug));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return security;
    }

    Map<String, Security.PercentageUsedTuple> getListForNode(JsonObject oNode, boolean debugLog) {
        Map<String, Security.PercentageUsedTuple> oResultList = new HashMap<String, Security.PercentageUsedTuple>();
        if (oNode != null) {
            JsonArray oArrayList = oNode.getAsJsonArray("list");
            Security e = new Security("isin");
            for (int i = 0; i < oArrayList.size(); i++) {
                JsonObject oNodeInsideArray = ((JsonObject) oArrayList.get(i));
                String strName = oNodeInsideArray.get("nameBreakdown").getAsString();
                Double nPercent = oNodeInsideArray.get("investmentPct").getAsDouble();
                if (!strName.equals("Barmittel") || oNode.get("nameFundsBreakdown").getAsString().equals("Instrument")) {
                    oResultList.put(strName, e.new PercentageUsedTuple(nPercent, false));
                    if (debugLog) {
                        System.out.printf("name: %s; Percentage: %s%%\n", strName, DECIMAL_FORMAT.format(nPercent));
                    }
                }
            }
        }
        return oResultList;
    }

    String readStringFromURL(String requestURL) {
        Scanner scanner = null;
        String result = "";
        try {
            scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString());
            scanner.useDelimiter("\\A");
            result = scanner.hasNext() ? scanner.next() : "";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert scanner != null;
            scanner.close();
        }
        return result;
    }

}
