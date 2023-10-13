package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.Security;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import xml.XmlHelper;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Logger;

public class SecurityService {
    public static final String ONVISTA_DETAILS_REQUEST_URL = "https://www.onvista.de/etf/anlageschwerpunkt/";
    public static final String ONVISTA_URL = "https://www.onvista.de";
    private static final Logger logger = Logger.getLogger(SecurityService.class.getCanonicalName());
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    XmlHelper xmlHelper = new XmlHelper();

    public List<Security> processSecurities(NodeList oAllSecurities) {
        List<Security> securities = new ArrayList<>();
        for (int i = 0; i < oAllSecurities.getLength(); i++) {
            securities.add(processSecurity((Element) oAllSecurities.item(i)));
        }

        return securities;
    }

    Security processSecurity(Element securitiesElement) {
        Security security = null;
        String isin = xmlHelper.getTextContent(securitiesElement, "isin");
        String isRetired = xmlHelper.getTextContent(securitiesElement, "isRetired");

        if (!isin.isEmpty() && "false".equals(isRetired)) {
            logger.info("Fetching data for \"" + isin + "\"...");
            security = createSecurity(isin);
            String name = xmlHelper.getTextContent(securitiesElement, "name");
            if (!name.isEmpty()) {
                security.setName(name);
            }
            logger.info(security.toString() + " - done!");
        }

        return security;
    }


    boolean isETF(String strResponseFromFirstCall) {
        return strResponseFromFirstCall.startsWith("/etf/anlageschwerpunkt");
    }

    boolean isFond(String strResponseFromFirstCall) {
        return strResponseFromFirstCall.startsWith("/fonds/anlageschwerpunkt");
    }

    Security createSecurity(String strIsin) {
        Security security = new Security(strIsin);
        try {
            String detailsRequestPath = readStringFromURL(ONVISTA_DETAILS_REQUEST_URL + strIsin);

            boolean isEftOrFond = isETF(detailsRequestPath)
                    || isFond(detailsRequestPath);
            logger.fine(" - is ETF or Fond: " + isEftOrFond);
            if (isEftOrFond) {
                JsonObject breakdownsNode = getBreakDownForSecurity(detailsRequestPath);

                // parsing holdings
                if (breakdownsNode != null) {
                    Map<String, Security.PercentageUsedTuple> oListForHoldings = getHoldingPercentageMap(breakdownsNode, security);
                    security.setHoldings(oListForHoldings);

                    // parsing branches
                    security.setBranches(getMappedPercentageForNode(breakdownsNode.getAsJsonObject("branchBreakdown"), security));

                    // parsing currency
                    security.setCurrencies(getMappedPercentageForNode(breakdownsNode.getAsJsonObject("currencyBreakdown"), security));

                    // parsing instrument
                    security.setInstruments(getMappedPercentageForNode(breakdownsNode.getAsJsonObject("instrumentBreakdown"), security));

                    // parsing country
                    security.setCountries(getMappedPercentageForNode(breakdownsNode.getAsJsonObject("countryBreakdown"), security));
                }
            }
        } catch (Exception e) {
            logger.warning("Error loading details for " + strIsin + ": " + e.getMessage());
        }
        return security;
    }

    JsonObject getBreakDownForSecurity(String detailsRequestPath) {
        String htmlPageAnlageschwerpunkt = readStringFromURL(ONVISTA_URL + detailsRequestPath);
        String jsonResponsePart = extractJsonPartFromHtml(htmlPageAnlageschwerpunkt);

        JsonObject rootObj = JsonParser.parseString(jsonResponsePart).getAsJsonObject();
        return rootObj.getAsJsonObject("props").getAsJsonObject("pageProps").getAsJsonObject("data").getAsJsonObject("breakdowns");
    }

    private String extractJsonPartFromHtml(String htmlPageAnlageschwerpunkt) {
        String[] splitResponse = htmlPageAnlageschwerpunkt.split("<script id=\"__NEXT_DATA__\" type=\"application/json\">");
        return splitResponse[1].split("</script>")[0];
    }

    Map<String, Security.PercentageUsedTuple> getMappedPercentageForNode(JsonObject oNode, Security security) {
        Map<String, Security.PercentageUsedTuple> oResultList = new HashMap<>();
        if (oNode != null) {
            JsonArray oArrayList = oNode.getAsJsonArray("list");
            for (int i = 0; i < oArrayList.size(); i++) {
                JsonObject oNodeInsideArray = ((JsonObject) oArrayList.get(i));
                String strName = oNodeInsideArray.get("nameBreakdown").getAsString();
                Double nPercent = oNodeInsideArray.get("investmentPct").getAsDouble();
                String nameFundsBreakdown = oNode.get("nameFundsBreakdown").getAsString();
                logger.finer("nameBreakdown: " + strName + " - nameFundsBreakdown: " + nameFundsBreakdown);
                if (!strName.equals("Barmittel") || nameFundsBreakdown.equals("Instrument")) {
                    oResultList.put(strName, security.new PercentageUsedTuple(nPercent, false));
                    logger.fine(String.format("name: %s; Percentage: %s%%", strName, DECIMAL_FORMAT.format(nPercent)));
                }
            }
        }
        return oResultList;
    }

    Map<String, Security.PercentageUsedTuple> getHoldingPercentageMap(JsonObject breakdownsNode, Security security) {
        Map<String, Security.PercentageUsedTuple> oListForHoldings = new HashMap<>();
        JsonObject fundsHoldingList = breakdownsNode.getAsJsonObject("fundsHoldingList");
        JsonArray holdingListArray = fundsHoldingList != null ? fundsHoldingList.getAsJsonArray("list") : new JsonArray();

        for (int i = 0; i < holdingListArray.size(); i++) {
            JsonObject oHolding = ((JsonObject) holdingListArray.get(i));
            String strHoldingName = oHolding.getAsJsonObject("instrument").get("name").getAsString();
            Double nHoldingPercent = oHolding.get("investmentPct").getAsDouble();
            logger.fine(String.format("Holding: %s; Percentage: %s%%", strHoldingName, DECIMAL_FORMAT.format(nHoldingPercent)));
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
