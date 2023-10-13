package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import enums.SecurityType;
import models.Security;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import xml.XmlHelper;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

public class SecurityService {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    public static final String ONVISTA_DETAILS_REQUEST_URL = "https://www.onvista.de/etf/anlageschwerpunkt/";
    public static final String ONVISTA_URL = "https://www.onvista.de";
    XmlHelper xmlHelper = new XmlHelper();

    public List<Security> processSecurities(NodeList oAllSecurities, List<SecurityType> requestedSecurityTypes) {
        List<Security> securities = new ArrayList<>();
        for (int i = 0; i < oAllSecurities.getLength(); i++) {
            securities.add(processSecurity((Element) oAllSecurities.item(i), requestedSecurityTypes));
        }

        return securities;
    }

    Security processSecurity(Element securitiesElement, List<SecurityType> requestedSecurityTypes) {
        Security security = null;
        String isin = xmlHelper.getTextContent(securitiesElement, "isin");
        String isRetired = xmlHelper.getTextContent(securitiesElement, "isRetired");

        if (!isin.isEmpty() && "false".equals(isRetired)) {
            System.out.print("Fetching data for \"" + isin + "\"... - ");
            security = createSecurity(isin, requestedSecurityTypes);
            String name = xmlHelper.getTextContent(securitiesElement, "name");
            if (!name.isEmpty()) {
                security.setName(name);
            }
            System.out.print(" done!\n");
        }
        
        return security;
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
            String detailsRequestPath = readStringFromURL(ONVISTA_DETAILS_REQUEST_URL + strIsin);
//            String name = detailsRequestPath.split("/")[2];
//            System.out.print(detailsRequestPath + " (" + name + ")");
//            security.setName(name);

            // Check if ETF or fond
            boolean isEftOrFond = isETF(detailsRequestPath, requestedSecurityTypes)
                    || isFond(detailsRequestPath, requestedSecurityTypes);
            System.out.println(" - is ETF or Fond: " + isEftOrFond);
            if (isEftOrFond) {
                JsonObject breakdownsNode = getBreakDownForSecurity(detailsRequestPath);

                // parsing holdings
                if (breakdownsNode != null) {
                    Map<String, Security.PercentageUsedTuple> oListForHoldings = getHoldingPercentageMap(breakdownsNode, !debug, security);
                    security.setHoldings(oListForHoldings);

                    // parsing branches
                    security.setBranches(getMappedPercentageForNode(breakdownsNode.getAsJsonObject("branchBreakdown"), debug, security));

                    // parsing currency
                    security.setCurrencies(getMappedPercentageForNode(breakdownsNode.getAsJsonObject("currencyBreakdown"), debug, security));

                    // parsing instrument
                    security.setInstruments(getMappedPercentageForNode(breakdownsNode.getAsJsonObject("instrumentBreakdown"), debug, security));

                    // parsing country
                    security.setCountries(getMappedPercentageForNode(breakdownsNode.getAsJsonObject("countryBreakdown"), debug, security));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return security;
    }

    JsonObject getBreakDownForSecurity(String detailsRequestPath) {
        String htmlPageAnlageschwerpunkt = readStringFromURL(ONVISTA_URL + detailsRequestPath);
        String jsonResponsePart = extractJsonPartFromHtml(htmlPageAnlageschwerpunkt);

        JsonObject rootObj = JsonParser.parseString(jsonResponsePart).getAsJsonObject();
        JsonObject breakdownsNode = rootObj.getAsJsonObject("props").getAsJsonObject("pageProps").getAsJsonObject("data").getAsJsonObject("breakdowns");
        return breakdownsNode;
    }

    private String extractJsonPartFromHtml(String htmlPageAnlageschwerpunkt) {
        String[] splitResponse = htmlPageAnlageschwerpunkt.split("<script id=\"__NEXT_DATA__\" type=\"application/json\">");
        String jsonResponsePart = splitResponse[1].split("</script>")[0];
        return jsonResponsePart;
    }

    Map<String, Security.PercentageUsedTuple> getMappedPercentageForNode(JsonObject oNode, boolean debugLog, Security security) {
        Map<String, Security.PercentageUsedTuple> oResultList = new HashMap<String, Security.PercentageUsedTuple>();
        if (oNode != null) {
            JsonArray oArrayList = oNode.getAsJsonArray("list");
            for (int i = 0; i < oArrayList.size(); i++) {
                JsonObject oNodeInsideArray = ((JsonObject) oArrayList.get(i));
                String strName = oNodeInsideArray.get("nameBreakdown").getAsString();
                Double nPercent = oNodeInsideArray.get("investmentPct").getAsDouble();
                if (!strName.equals("Barmittel") || oNode.get("nameFundsBreakdown").getAsString().equals("Instrument")) {
                    oResultList.put(strName, security.new PercentageUsedTuple(nPercent, false));
                    if (debugLog) {
                        System.out.printf("name: %s; Percentage: %s%%\n", strName, DECIMAL_FORMAT.format(nPercent));
                    }
                }
            }
        }
        return oResultList;
    }

    Map<String, Security.PercentageUsedTuple> getHoldingPercentageMap(JsonObject breakdownsNode, boolean debug, Security security) {
        Map<String, Security.PercentageUsedTuple> oListForHoldings = new HashMap<>();
        JsonObject fundsHoldingList = breakdownsNode.getAsJsonObject("fundsHoldingList");
        JsonArray holdingListArray = fundsHoldingList != null ? fundsHoldingList.getAsJsonArray("list") : new JsonArray();

        for (int i = 0; i < holdingListArray.size(); i++) {
            JsonObject oHolding = ((JsonObject) holdingListArray.get(i));
            String strHoldingName = oHolding.getAsJsonObject("instrument").get("name").getAsString();
            Double nHoldingPercent = oHolding.get("investmentPct").getAsDouble();
            if (debug)
                System.out.printf("Holding: %s; Percentage: %s%%\n", strHoldingName, DECIMAL_FORMAT.format(nHoldingPercent));
            oListForHoldings.put(strHoldingName, security.new PercentageUsedTuple(nHoldingPercent, false));
        }
        return oListForHoldings;
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
