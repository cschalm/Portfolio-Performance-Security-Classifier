package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import models.Security;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xml.XmlHelper;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static constants.PathConstants.CACHE_PATH;

public class SecurityService {
    private static final Logger logger = Logger.getLogger(SecurityService.class.getCanonicalName());
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    XmlHelper xmlHelper = new XmlHelper();
    private String cachePath = CACHE_PATH;

    public SecurityService() {
    }

    public SecurityService(String cachePath) {
        this.cachePath = cachePath;
    }

    public List<Security> processSecurities(NodeList allSecurities) {
        List<Security> securities = new ArrayList<>();
        for (int i = 0; i < allSecurities.getLength(); i++) {
            Security security = processSecurity((Element) allSecurities.item(i), i);
            if (security != null) securities.add(security);
        }
        securities.sort(new Security.SecurityComparator());

        return securities;
    }

    Security processSecurity(Element securitiesElement, int indexInPortfolio) {
        String isin = xmlHelper.getTextContent(securitiesElement, "isin");
        String isRetired = xmlHelper.getTextContent(securitiesElement, "isRetired");

        if (!isin.isEmpty() && "false".equals(isRetired)) {
            Security security = createSecurity(isin, indexInPortfolio);
            String name = xmlHelper.getTextContent(securitiesElement, "name");
            if ((security.getName() == null || security.getName().isEmpty()) && !name.isEmpty()) {
                security.setName(name);
            }
            return security;
        }

        return null;
    }

    Security createSecurity(String strIsin, int indexInPortfolio) {
        Security security = new Security(strIsin, indexInPortfolio);
        try {
            SecurityDetails securityDetails = new SecurityDetails(cachePath, strIsin);

            boolean isEftOrFond = securityDetails.isETF() || securityDetails.isFonds();
            logger.fine(" - is ETF or Fond: " + isEftOrFond);
            security.setFond(isEftOrFond);
            if (isEftOrFond) {
                JsonObject breakdownsNode = securityDetails.getBreakDownForSecurity();

                if (breakdownsNode != null) {
                    // parsing holdings
                    Map<String, Double> oListForHoldings = getHoldingPercentageMap(breakdownsNode);
                    security.setHoldings(oListForHoldings);

                    // parsing branches
                    security.setIndustries(getMappedPercentageForNode(breakdownsNode.getAsJsonObject("branchBreakdown")));

                    // parsing country
                    security.setCountries(getMappedPercentageForNode(breakdownsNode.getAsJsonObject("countryBreakdown")));
                }
            } else {
                String industry = securityDetails.getIndustry();
                Map<String, Double> industriesMap = new HashMap<>();
                industriesMap.put(industry, 100.0);
                security.setIndustries(industriesMap);

                String country = securityDetails.getCountryForSecurity();
                Map<String, Double> countryMap = new HashMap<>();
                countryMap.put(country, 100.0);
                security.setCountries(countryMap);

                String companyName = securityDetails.getName();
                security.setName(companyName);
                Map<String, Double> holdingsMap = new HashMap<>();
                holdingsMap.put(companyName, 100.0);
                security.setHoldings(holdingsMap);

                logger.fine("Setting name \"" + companyName + "\" and industry \"" + industry + "\" and country \"" + country + "\" to security: " + security);
            }
        } catch (Exception e) {
            logger.warning("Error loading details for " + strIsin + " from " + cachePath + ": " + e.getMessage());
        }
        return security;
    }

    Map<String, Double> getMappedPercentageForNode(JsonObject oNode) {
        Map<String, Double> oResultList = new HashMap<>();
        if (oNode != null) {
            JsonArray oArrayList = oNode.getAsJsonArray("list");
            for (int i = 0; i < oArrayList.size(); i++) {
                JsonObject oNodeInsideArray = ((JsonObject) oArrayList.get(i));
                String strName = oNodeInsideArray.get("nameBreakdown").getAsString();
                Double nPercent = oNodeInsideArray.get("investmentPct").getAsDouble();
                String nameFundsBreakdown = oNode.get("nameFundsBreakdown").getAsString();
                logger.finer("nameBreakdown: " + strName + " - nameFundsBreakdown: " + nameFundsBreakdown);
                if (!strName.equals("Barmittel") || nameFundsBreakdown.equals("Instrument")) {
                    oResultList.put(strName, nPercent);
                    logger.fine(String.format("name: %s; Percentage: %s%%", strName, DECIMAL_FORMAT.format(nPercent)));
                }
            }
        }
        return oResultList;
    }

    Map<String, Double> getHoldingPercentageMap(JsonObject breakdownsNode) {
        Map<String, Double> oListForHoldings = new HashMap<>();
        JsonObject fundsHoldingList = breakdownsNode.getAsJsonObject("fundsHoldingList");
        JsonArray holdingListArray = fundsHoldingList != null ? fundsHoldingList.getAsJsonArray("list") : new JsonArray();

        for (int i = 0; i < holdingListArray.size(); i++) {
            JsonObject oHolding = ((JsonObject) holdingListArray.get(i));
            String strHoldingName = oHolding.getAsJsonObject("instrument").get("name").getAsString();
            Double nHoldingPercent = oHolding.get("investmentPct").getAsDouble();
            logger.fine(String.format("Holding: %s; Percentage: %s%%", strHoldingName, DECIMAL_FORMAT.format(nHoldingPercent)));
            oListForHoldings.put(strHoldingName, nHoldingPercent);
        }
        return oListForHoldings;
    }

    public void removeOldPrices(NodeList allSecurities) {
        int removedCount = 0;
        LocalDate olderThan = LocalDate.now().minusYears(2);
        for (int i = 0; i < allSecurities.getLength(); i++) {
            removedCount += removeOldPrices((Element) allSecurities.item(i), olderThan);
        }
        logger.info("Removed " + removedCount + " old prices before " + olderThan.toString());
    }

    int removeOldPrices(Element securitiesElement, LocalDate olderThan) {
        int removedCount = 0;
        boolean isRetired = Boolean.parseBoolean(xmlHelper.getTextContent(securitiesElement, "isRetired"));
        Element prices = xmlHelper.getFirstChildElementWithNodeName(securitiesElement, "prices");
        if (prices == null) return removedCount;
        NodeList children = prices.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE && children.item(i).getNodeName().equals("price")) {
                Element price = (Element) children.item(i);
                if (isRetired) {
                    prices.removeChild(price);
                    removedCount++;
                    continue;
                }
                // else
                NamedNodeMap attributes = price.getAttributes();
                if (attributes != null) {
                    Node timeAttribute = attributes.getNamedItem("t");
                    if (timeAttribute != null) {
                        String dateString = timeAttribute.getNodeValue().trim();
                        LocalDate date = LocalDate.parse(dateString);
                        if (date.isBefore(olderThan)) {
                            prices.removeChild(price);
                            removedCount++;
                        }
                    }
                }
            }
        }

        return removedCount;
    }

}
