package xml;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import models.Security;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static constants.PathConstants.LOGS_PATH;

public class XmlFileWriter {
    private static final Logger logger = Logger.getLogger(XmlFileWriter.class.getCanonicalName());
    // create random object - reuse this as often as possible
    Random random = new Random();
    XmlHelper xmlHelper = new XmlHelper();

    public void writeXml(Document doc, String fileName) throws TransformerException, FileNotFoundException {
        FileOutputStream output = new FileOutputStream(fileName);
        writeXml(doc, output);
    }

    private void writeXml(Document doc, OutputStream output) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        // https://mkyong.com/java/pretty-print-xml-with-java-dom-and-xslt/
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);
    }

    public void updateXml(Document portfolioDocument, List<Security> allSecurities, SecurityDetailsCache securityDetailsCache) {
        try {
            NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
            for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
                Node taxonomyNode = listOfTaxonomies.item(i);
                if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element taxonomyElement = (Element) taxonomyNode;
                    String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                    logger.info("taxonomyName: " + taxonomyName);

                    if (taxonomyName.equals("Regionen")) {
                        JsonArray importedRegions = importRegions(portfolioDocument, allSecurities, securityDetailsCache.getCachedCountries(), taxonomyElement);
                        securityDetailsCache.setCachedCountries(importedRegions);
                    }

                    if (taxonomyName.equals("Branchen (GICS)")) {
                        JsonArray importedBranches = importBranches(portfolioDocument, allSecurities, securityDetailsCache.getCachedBranches(), taxonomyElement);
                        securityDetailsCache.setCachedBranches(importedBranches);
                    }

                    if (taxonomyName.equals("Top Ten")) {
                        JsonArray importedTopTen = importTopTen(portfolioDocument, allSecurities, securityDetailsCache.getCachedTopTen(), taxonomyElement);
                        securityDetailsCache.setCachedTopTen(importedTopTen);
                    }
                }
            }

            if (logger.isLoggable(Level.FINE)) {
                for (Security security : allSecurities) {
                    if (security != null) {
                        logger.fine(security.toString());
                        logger.fine(security.getIsin() + ": unused countries: " + security.getUnusedCountries() + "; \"Andere\": " + security.getPercentageOfCountry("Andere"));
                        logger.fine(security.getIsin() + ": unused branches: " + security.getUnusedBranches() + "; \"Andere\": " + security.getPercentageOfBranch("Andere"));
                    }
                }
            }

            // write all saved triples to avoid importing the same assignments several times for each run
            securityDetailsCache.save();

        } catch (IOException e) {
            logger.warning("Error updating XML: " + e.getMessage());
        }

    }

    JsonArray importTopTen(Document portfolioDocument, List<Security> allSecurities, JsonArray cachedTopTen, Element taxonomyElement) {
        logger.info("Importing Top Ten...");
        Element oRootOfTopTen = (Element) taxonomyElement.getElementsByTagName("root").item(0);

        JsonArray importedTopTen = new JsonArray();
        NodeList oAllChildren = oRootOfTopTen.getChildNodes();
        Element childrenNode = portfolioDocument.createElement("children");
        for (int nNodeIndex = 0; nNodeIndex < oAllChildren.getLength(); nNodeIndex++) {
            Node item = oAllChildren.item(nNodeIndex);
            if (item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().equals("children")) {
                childrenNode = (Element) item;
            }
        }

        TreeMap<String, List<String>> allStockNames = collectAllStockNames(allSecurities);

        for (String stockName : allStockNames.keySet()) {
            logger.fine("Stockname: " + stockName);

            //setting each stock as own classification
            Element classificationNodeForStock = portfolioDocument.createElement("classification");

            Element id = portfolioDocument.createElement("id");
            id.setTextContent(UUID.randomUUID().toString());

            Element name = portfolioDocument.createElement("name");
            name.setTextContent(stockName);

            Element color = portfolioDocument.createElement("color");
            // create a big random number - maximum is ffffff (hex) = 16777215 (dez)
            int nextInt = random.nextInt(0xffffff + 1);
            // format it as hexadecimal string (with hashtag and leading zeros)
            String colorCode = String.format("#%06x", nextInt);
            color.setTextContent(colorCode);

            Element parent = portfolioDocument.createElement("parent");
            parent.setAttribute("reference", "../../..");

            Element children = portfolioDocument.createElement("children");

            Element assignments = portfolioDocument.createElement("assignments");

            Element weight = portfolioDocument.createElement("weight");
            weight.setTextContent("10000");

            Element rank = portfolioDocument.createElement("rank");
            rank.setTextContent("0");

            classificationNodeForStock.appendChild(id);
            classificationNodeForStock.appendChild(name);
            classificationNodeForStock.appendChild(color);
            classificationNodeForStock.appendChild(parent);
            classificationNodeForStock.appendChild(children);
            classificationNodeForStock.appendChild(assignments);
            classificationNodeForStock.appendChild(weight);
            classificationNodeForStock.appendChild(rank);

            int nETFAppearance = 0;
            int indexEtf = 0;
            for (Security security : allSecurities) {
                if (security != null) {
                    // find security that contains the current stock identified by any similar name
                    if (security.getHoldings().containsKey(stockName)) {
                        // primary name
                        nETFAppearance = addTopTenAssignment(portfolioDocument, cachedTopTen, stockName, security, indexEtf, nETFAppearance, assignments, importedTopTen);
                    } else {
                        // alternative names
                        List<String> alternativeNames = allStockNames.get(stockName);
                        for (String alternativeName : alternativeNames) {
                            if (security.getHoldings().containsKey(alternativeName)) {
                                nETFAppearance = addTopTenAssignment(portfolioDocument, cachedTopTen, alternativeName, security, indexEtf, nETFAppearance, assignments, importedTopTen);
                            }
                        }
                    }
                }
                indexEtf++;
            }
            // only add classification if it has assignments; no assignments happen, if the ETF were added in previous runs and is written into the save file
            if (assignments.hasChildNodes()) {
                childrenNode.appendChild(classificationNodeForStock);
            }
        }
        logger.info(" - done!");

        return importedTopTen;
    }

    private int addTopTenAssignment(Document portfolioDocument, JsonArray cachedTopTen, String stockName, Security security, int indexEtf, int nETFAppearance, Element assignments, JsonArray importedTopTen) {
        Element investmentVehicle = portfolioDocument.createElement("investmentVehicle");
        investmentVehicle.setAttribute("class", "security");
        investmentVehicle.setAttribute("reference", "../../../../../../../../securities/security[" + (indexEtf + 1) + "]");

        int percentage = (int) Math.ceil(security.getPercentageOfHolding(stockName) * 100.0);

        boolean found = false;
        for (int i = 0; i < cachedTopTen.size(); i++) {
            JsonObject topTen = cachedTopTen.get(i).getAsJsonObject();
            if (jsonObjectEqualsInWeightAndIsinAndClassification(topTen, security.getIsin(), percentage, stockName)) {
                found = true;
                break;
            }
        }

        if (!found) {
            // verify that this stock was not imported by an alternative name before
            for (int i = 0; i < importedTopTen.size(); i++) {
                JsonObject topTen = importedTopTen.get(i).getAsJsonObject();
                if (jsonObjectEqualsInWeightAndIsinAndClassification(topTen, security.getIsin(), percentage, stockName)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            createAssignmentAndJson(portfolioDocument, stockName, security.getIsin(), nETFAppearance, assignments, importedTopTen, percentage, investmentVehicle);
            nETFAppearance++;
        }
        return nETFAppearance;
    }

    private void createAssignmentAndJson(Document doc, String stockName, String isin, int rank, Element assignments, JsonArray importParent, int weight, Element investmentVehicle) {
        appendNewAssignment(doc, rank, assignments, weight, investmentVehicle);
        addNewJsonSecurity(importParent, stockName, isin, weight);
    }

    private void addNewJsonSecurity(JsonArray importParent, String classification, String isin, int weight) {
        JsonObject imported = new JsonObject();
        imported.addProperty("weight", weight);
        imported.addProperty("isin", isin);
        imported.addProperty("classification", classification);
        importParent.add(imported);
    }

    private void appendNewAssignment(Document doc, int rank, Element assignments, int weight, Element investmentVehicle) {
        Element assignment = createAssignment(doc, rank + 1, weight);
        assignment.appendChild(investmentVehicle);
        assignments.appendChild(assignment);
    }

    Element createAssignment(Document doc, int rank, int weight) {
        Element weightOfETF = doc.createElement("weight");
        weightOfETF.setTextContent(Integer.toString(weight));

        Element rankOfETF = doc.createElement("rank");
        rankOfETF.setTextContent(Integer.toString(rank));

        Element assignment = doc.createElement("assignment");
        assignment.appendChild(weightOfETF);
        assignment.appendChild(rankOfETF);

        return assignment;
    }

    TreeMap<String, List<String>> collectAllStockNames(List<Security> allSecurities) {
        TreeSet<String> allStockNames = new TreeSet<>();
        for (Security security : allSecurities) {
            if (security != null) {
                Map<String, Security.PercentageUsedTuple> holdings = security.getHoldings();
                allStockNames.addAll(holdings.keySet());
            }
        }
        logger.fine("All Stocknames:");
        for (String name : allStockNames) {
            logger.fine(name);
        }
        return reduceSimilarStrings(allStockNames);
    }

    TreeMap<String, List<String>> reduceSimilarStrings(Collection<String> input) {
        TreeMap<String, List<String>> result = new TreeMap<>();
        LevenshteinDistance distance = LevenshteinDistance.getDefaultInstance();
        for (String inputName : input) {
            if (result.isEmpty()) {
                // very first entry
                result.put(inputName, new ArrayList<>());
            } else {
                boolean found = false;
                // check if similar name already exists in map
                if (!inputName.startsWith("Vanguard")) {
                    for (String existingName : result.keySet()) {
                        Integer dist = distance.apply(inputName.toLowerCase(), existingName.toLowerCase());
                        if (dist <= 20) {
                            if (StringUtils.indexOfDifference(inputName.toLowerCase(), existingName.toLowerCase()) >= 5) {
                                // similar name exists, add alternative name to list
                                result.get(existingName).add(inputName);
                                found = true;
                                break;
                            }
                        }
                    }
                }
                if (!found) {
                    result.put(inputName, new ArrayList<>());
                }
            }
        }
        logger.fine("Reduced Stocknames:");
        for (String name : result.keySet()) {
            logger.fine(name);
        }
        return result;
    }


    JsonArray importBranches(Document portfolioDocument, List<Security> allSecurities, JsonArray cachedBranches, Element taxonomyElement) throws FileNotFoundException {
        logger.info("Importing branches...");
        NodeList allBranchesFromPortfolioNodeList = taxonomyElement.getElementsByTagName("classification");

        JsonArray importedBranches = new JsonArray();
        Map<String, NodeRankTuple> branchNameFromPortfolioToNodeMap = new HashMap<>();
        for (int indexBranch = 0; indexBranch < allBranchesFromPortfolioNodeList.getLength(); indexBranch++) {
            Node branchFromPortfolioNode = allBranchesFromPortfolioNodeList.item(indexBranch);
            if (branchFromPortfolioNode.getNodeType() == Node.ELEMENT_NODE) {
                String branchNameFromPortfolio = xmlHelper.getTextContent((Element) branchFromPortfolioNode, "name");
                logger.fine("Importing branch " + branchNameFromPortfolio);
                branchNameFromPortfolioToNodeMap.put(branchNameFromPortfolio, new NodeRankTuple(branchFromPortfolioNode, 0));
            }
        }
        int indexEtf = 0;
        for (Security security : allSecurities) {
            if (security == null) {
                indexEtf++;
                continue;
            }
            logger.fine("Security: " + security);
            String[] branchNamesFromSecurity = security.getAllBranches();
            if (branchNamesFromSecurity != null && branchNamesFromSecurity.length > 0) {

                StringBuilder strMatchingStringForFile = new StringBuilder();
                for (String branchNameFromSecurity : branchNamesFromSecurity) {
                    String optimizedBranchNameFromSecurity = optimizeBranchNameFromSecurity(branchNameFromSecurity);
                    if ("DE000TUAG505".equalsIgnoreCase(security.getIsin())) {
                        // Tui is classified as "Sonstige Branchen" ?!?
                        optimizedBranchNameFromSecurity = "Hotels, Urlaubsanlagen & Kreuzfahrtlinien";
                    }
                    if ("US04010L1035".equalsIgnoreCase(security.getIsin())) {
                        // Ares Capital Markets has no classification on Onvista ?!?
                        optimizedBranchNameFromSecurity = "Kapitalmärkte";
                    }
                    if ("DE0008402215".equalsIgnoreCase(security.getIsin())) {
                        // Hannover Rück is classified as "Sonstige Branchen" ?!?
                        optimizedBranchNameFromSecurity = "Rückversicherungen";
                    }
                    // skip not matching branches
                    if (optimizedBranchNameFromSecurity.isEmpty()) continue;
                    String bestMatchingBranchName = "";
                    int currentLowestDistance = 1000;
                    for (String branchNameFromPortfolio : branchNameFromPortfolioToNodeMap.keySet()) {
                        int temp = levenshteinDistance(optimizedBranchNameFromSecurity, branchNameFromPortfolio);
                        if (temp < currentLowestDistance) {
                            bestMatchingBranchName = branchNameFromPortfolio;
                            currentLowestDistance = temp;
                        }
                    }

                    int nPercentage = (int) Math.ceil(security.getPercentageOfBranch(branchNameFromSecurity) * 100.0);

                    logger.fine("-> Branche (ETF / Fond) \"" + branchNameFromSecurity + "\" mit " + ((double) nPercentage / 100.0) + "% der Branche (PP) \"" + bestMatchingBranchName + "\" zugeordnet. (Distanz: " + currentLowestDistance + ")");

                    boolean alreadyAddedBefore = isContainedInCache(cachedBranches, security.getIsin(), nPercentage, bestMatchingBranchName, importedBranches);

                    if (nPercentage > 0 && !alreadyAddedBefore) {
                        Element assignment = portfolioDocument.createElement("assignment");
                        NodeRankTuple oTuple = branchNameFromPortfolioToNodeMap.get(bestMatchingBranchName);
                        Node branchNode = oTuple.oNode;

                        Element assignments = portfolioDocument.createElement("assignments");
                        Element investmentVehicle = portfolioDocument.createElement("investmentVehicle");

                        assignments = linkAssignmentsToInvestmentVehicle(branchNode, assignments, investmentVehicle, indexEtf);

                        Element weight = portfolioDocument.createElement("weight");
                        weight.setTextContent(Integer.toString(nPercentage));

                        Element rank = portfolioDocument.createElement("rank");
                        oTuple.nRank++;
                        rank.setTextContent(Integer.toString(oTuple.nRank));

                        assignment.appendChild(investmentVehicle);
                        assignment.appendChild(weight);
                        assignment.appendChild(rank);

                        assignments.appendChild(assignment);
                        strMatchingStringForFile.append("-> Branche (ETF / Fond) \"").append(branchNameFromSecurity).append("\" mit ").append((double) nPercentage / 100.0).append("% der Branche (PP) \"").append(bestMatchingBranchName).append("\" zugeordnet. Distanz: ").append(currentLowestDistance).append(")\n");

                        addNewJsonSecurity(importedBranches, bestMatchingBranchName, security.getIsin(), nPercentage);
                    }
                }
                if (strMatchingStringForFile.length() > 0) {
                    File logsDir = new File(LOGS_PATH);
                    //noinspection ResultOfMethodCallIgnored
                    logsDir.mkdirs();
                    PrintWriter out = new PrintWriter(LOGS_PATH + security.getName() + ".txt");
                    out.print(strMatchingStringForFile);
                    out.close();
                }
            }
            indexEtf++;
        }
        logger.info(" - done!");

        return importedBranches;
    }

    private Element linkAssignmentsToInvestmentVehicle(Node node, Element assignments, Element investmentVehicle, int indexEtf) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE && children.item(i).getNodeName().equals("assignments")) {
                assignments = (Element) children.item(i);
            }
        }

        int stepsToRoot = returnRootSteps(assignments) + 3;

        investmentVehicle.setAttribute("reference", "../".repeat(Math.max(0, stepsToRoot)) + "securities/security[" + (indexEtf + 1) + "]");
        investmentVehicle.setAttribute("class", "security");

        return assignments;
    }

    private boolean isContainedInCache(JsonArray cache, String isin, int weight, String classification, JsonArray parentArray) {
        boolean found = false;
        // checking if the triple was added in an earlier run of the tool (therefore skip it -> no double entry AND it may have been moved
        for (int index = 0; index < cache.size(); index++) {
            JsonObject cachedJson = cache.get(index).getAsJsonObject();
            if (jsonObjectEqualsInWeightAndIsinAndClassification(cachedJson, isin, weight, classification)) {
                found = true;
                JsonObject oSavingTriple = new JsonObject();
                oSavingTriple.addProperty("weight", weight);
                oSavingTriple.addProperty("isin", isin);
                oSavingTriple.addProperty("classification", classification);
                parentArray.add(oSavingTriple);
                break;
            }
        }
        return found;
    }

    private boolean jsonObjectEqualsInWeightAndIsinAndClassification(JsonObject json, String isin, int weight, String classification) {
        return json.get("weight").getAsInt() == weight &&
                json.get("isin").getAsString().equals(isin) &&
                json.get("classification").getAsString().equals(classification);
    }

    /*
        Some names from the official data from a security might not fit well into the schema from
        Portfolio Performance and should be "optimized" - some are misspelled
     */
    String optimizeBranchNameFromSecurity(String branchNameFromSecurity) {
        String result = branchNameFromSecurity;
        switch (branchNameFromSecurity) {
            case "Telekomdienste":
                result = "Telekommunikationsdienste";
                break;
            case "diverse Branchen":
            case "Sonstige Branchen":
                result = "";
                break;
            case "Konsumgüter zyklisch":
                result = "Nicht-Basiskonsumgüter";
                break;
            case "Rohstoffe":
                result = "Roh-, Hilfs- & Betriebsstoffe";
                break;
            case "Computerherstellung":
                result = "Hardware Technologie, Speicherung & Peripherie";
                break;
            case "Fahrzeugbau":
                result = "Automobilbranche";
                break;
            case "Halbleiterelektronik":
                result = "Halbleiter";
                break;
            case "Baumaterialien/Baukomponenten":
                result = "Baumaterialien";
                break;
            case "Halbleiter Ausstattung":
                result = "Geräte zur Halbleiterproduktion";
                break;
            case "Vesorger/Strom konventionell/ Enegiefirmen":
            case "Versorger/Strom konventionell/ Enegiefirmen":
            case "Versorger/Strom konventionell/ Energiefirmen":
            case "Versorger umfassend":
                result = "Multi-Versorger";
                break;
            case "Versorger/ erneuerbare Energie":
                result = "Unabhängige Energie- und Erneuerbare Elektrizitätshersteller";
                break;
            case "Bauwesen":
                result = "Bau- & Ingenieurswesen";
                break;
            case "Unterhaltungselektronik":
                result = "Verbraucherelektronik";
                break;
            default:
                break;
        }
        return result;
    }

    JsonArray importRegions(Document portfolioDocument, List<Security> allSecurities, JsonArray cachedCountries, Element taxonomyElement) {
        logger.info("Importing regions...");
        NodeList oListOfAllCountries = taxonomyElement.getElementsByTagName("classification");

        JsonArray importedRegions = new JsonArray();
        for (int indexCountry = 0; indexCountry < oListOfAllCountries.getLength(); indexCountry++) {
            Node countryNode = oListOfAllCountries.item(indexCountry);
            int nCountryAppearence = 0;
            if (countryNode.getNodeType() == Node.ELEMENT_NODE) {
                String strCountry = xmlHelper.getTextContent((Element) countryNode, "name");
                if (strCountry.equals("Vereinigte Staaten")) {
                    strCountry = "USA";
                }
                int indexEtf = 0;
                for (Security security : allSecurities) {
                    if (security != null) {
                        int nPercentage = (int) Math.ceil(security.getPercentageOfCountry(strCountry) * 100.0);

                        boolean alreadyAddedBefore = isContainedInCache(cachedCountries, security.getIsin(), nPercentage, strCountry, importedRegions);

                        if (nPercentage > 0 && !alreadyAddedBefore) {
                            //System.out.printf("Country: %s with more than 0.0 found in etf: %s\n", strCountry, allSecurities[indexEtf].getName());
                            Element assignments = portfolioDocument.createElement("assignments");
                            Element investmentVehicle = portfolioDocument.createElement("investmentVehicle");

                            assignments = linkAssignmentsToInvestmentVehicle(countryNode, assignments, investmentVehicle, indexEtf);

                            createAssignmentAndJson(portfolioDocument, strCountry, security.getIsin(), nCountryAppearence, assignments, importedRegions, nPercentage, investmentVehicle);
                            nCountryAppearence++;
                        }
                    }
                    indexEtf++;
                }
            }
        }
        logger.info(" - done!");

        return importedRegions;
    }

    private int returnRootSteps(Node node) {
        if (node.getParentNode().getNodeName().equals("client")) {
            return 0;
        } else {
            return 1 + returnRootSteps(node.getParentNode());
        }
    }

    private int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;
        lhs = lhs.toString().toLowerCase();
        rhs = rhs.toString().toLowerCase();
        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

    public static class NodeRankTuple {
        Node oNode;
        int nRank;

        public NodeRankTuple(Node node, int b) {
            this.oNode = node;
            this.nRank = b;
        }
    }

}
