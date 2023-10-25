package xml;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.Security;
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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Logger;

import static constants.PathConstants.*;

public class XmlFileWriter {
    private static final Logger logger = Logger.getLogger(XmlFileWriter.class.getCanonicalName());
    XmlHelper xmlHelper = new XmlHelper();

    private void writeXml(Document doc, String fileName) throws TransformerException, UnsupportedEncodingException, FileNotFoundException {
        FileOutputStream output = new FileOutputStream(fileName);
        writeXml(doc, output);
    }

    // write doc to output stream
    private void writeXml(Document doc, OutputStream output) throws TransformerException, UnsupportedEncodingException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        // https://mkyong.com/java/pretty-print-xml-with-java-dom-and-xslt/
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);
    }

    public void updateXml(Document portfolioDocument, Security[] allSecurities, String outputDirectory) {
        try {
            JsonObject cacheFileJson;
            JsonArray cachedCountries;
            JsonArray cachedBranches;
            JsonArray cachedTopTen;

            try {
                // if there is an entry in the cache-file, nothing is imported !!!
                cacheFileJson = JsonParser.parseReader(new FileReader(SAVE_FILE + ".bak")).getAsJsonObject();
                cachedCountries = cacheFileJson.get("countries").getAsJsonArray();
                cachedBranches = cacheFileJson.get("branches").getAsJsonArray();
                cachedTopTen = cacheFileJson.get("topten").getAsJsonArray();
            } catch (Exception e) {
                logger.finer("Cache of imports could not be read: " + e.getMessage());
                cachedCountries = new JsonArray();
                cachedBranches = new JsonArray();
                cachedTopTen = new JsonArray();
            }
            JsonObject newCacheFileJson = new JsonObject();

            NodeList listOfTaxonomies = portfolioDocument.getElementsByTagName("taxonomy");
            for (int i = 0; i < listOfTaxonomies.getLength(); i++) {
                Node taxonomyNode = listOfTaxonomies.item(i);
                if (taxonomyNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element taxonomyElement = (Element) taxonomyNode;
                    String taxonomyName = xmlHelper.getTextContent(taxonomyElement, "name");
                    logger.info("taxonomyName: " + taxonomyName);

                    // countries start
                    if (taxonomyName.equals("Regionen")) {
                        JsonArray importedRegions = importRegions(portfolioDocument, allSecurities, cachedCountries, taxonomyElement);
                        // adding all country triples to the "all" jsonObject (for performance split on country/region/etc
                        newCacheFileJson.add("countries", importedRegions);
                    }
                    // endof countries

                    // branches start
                    if (taxonomyName.equals("Branchen (GICS)")) {
                        JsonArray importedBranches = importBranches(portfolioDocument, allSecurities, cachedBranches, taxonomyElement);
                        // adding all country triples to the "all" jsonObject (for performance split on country/region/etc
                        newCacheFileJson.add("branches", importedBranches);
                    }
                    // end of branches

                    // top ten start
                    if (taxonomyName.equals("Top Ten")) {
                        JsonArray importedTopTen = importTopTen(portfolioDocument, allSecurities, cachedTopTen, taxonomyElement);
                        // adding all country triples to the "all" jsonObject (for performance split on country/region/etc
                        newCacheFileJson.add("topten", importedTopTen);
                    }
                    // endof top ten

                    for (Security security : allSecurities) {
                        if (security != null) {
                            logger.finest(security.getIsin() + ": " + security.getName());
                            //System.out.printf("name: %s; unused countries: %s; \"Andere\": %s%%\n", oEtf.getName(), oEtf.getUnusedCountries(), oEtf.getPercentageOfCountry("Andere"));
                            //System.out.printf("name: %s; unused branches: %s; \"Andere\": %s%%\n", oEtf.getName(), oEtf.getUnusedBranches(), oEtf.getPercentageOfBranch("Andere"));
                        }
                    }
                }
            }

            // write all saved triples to avoid importing the same assignments several times for each run
            PrintWriter savingImport = new PrintWriter(SAVE_FILE);
            savingImport.print(newCacheFileJson + "\n");
            savingImport.close();

            // output to console
            // writeXml(portfolioDocument, System.out);

            writeXml(portfolioDocument, outputDirectory + OUTPUT_FILE_NAME);

        } catch (IOException | TransformerException e) {
            logger.warning("Error updating XML: " + e.getMessage());
        }

    }

    JsonArray importTopTen(Document portfolioDocument, Security[] allSecurities, JsonArray cachedTopTen, Element taxonomyElement) {
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

        TreeSet<String> oListOfAllStocks = new TreeSet<>();

        for (Security security : allSecurities) {
            if (security != null) {
                Map<String, Security.PercentageUsedTuple> oHoldingsOfCurrentETF = security.getHoldings();
                oListOfAllStocks.addAll(oHoldingsOfCurrentETF.keySet());
            }
        }

        for (String strStockname : oListOfAllStocks) {
            logger.info("Stockname: " + strStockname);

            //setting each stock as own classification
            Element classificationNodeForStock = portfolioDocument.createElement("classification");

            Element id = portfolioDocument.createElement("id");
            id.setTextContent(UUID.randomUUID().toString());

            Element name = portfolioDocument.createElement("name");
            name.setTextContent(strStockname);

            Element color = portfolioDocument.createElement("color");
            color.setTextContent("#FFFFFF");

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

            int nETFAppearence = 0;
            for (int indexEtf = 0; indexEtf < allSecurities.length; indexEtf++) {
                if (allSecurities[indexEtf] != null && allSecurities[indexEtf].getHoldings().containsKey(strStockname)) {

                    Element assignment = portfolioDocument.createElement("assignment");
                    Element investmentVehicle = portfolioDocument.createElement("investmentVehicle");
                    investmentVehicle.setAttribute("class", "security");

                    investmentVehicle.setAttribute("reference", "../../../../../../../../securities/security[" + (indexEtf + 1) + "]");

                    int nPercentage = (int) Math.ceil(allSecurities[indexEtf].getPercentageOfHolding(strStockname) * 100.0);

                    boolean fSkipCurrentAdding = false;
                    for (int nIndexAddedStocks = 0; nIndexAddedStocks < cachedTopTen.size(); nIndexAddedStocks++) {
                        JsonObject oTriple = cachedTopTen.get(nIndexAddedStocks).getAsJsonObject();
                        if (oTriple.get("weight").getAsInt() == nPercentage && oTriple.get("isin").getAsString().equals(allSecurities[indexEtf].getIsin()) && oTriple.get("classification").getAsString().equals(strStockname)) {
                            fSkipCurrentAdding = true;
                            break;
                        }
                    }

                    if (!fSkipCurrentAdding) {
                        Element weightOfETF = portfolioDocument.createElement("weight");
                        weightOfETF.setTextContent(Integer.toString(nPercentage));

                        Element rankOfETF = portfolioDocument.createElement("rank");
                        nETFAppearence++;
                        rankOfETF.setTextContent(Integer.toString(nETFAppearence));

                        assignment.appendChild(investmentVehicle);
                        assignment.appendChild(weightOfETF);
                        assignment.appendChild(rankOfETF);

                        assignments.appendChild(assignment);
                    }
                    JsonObject oSavingTriple = new JsonObject();
                    oSavingTriple.addProperty("weight", nPercentage);
                    oSavingTriple.addProperty("isin", allSecurities[indexEtf].getIsin());
                    oSavingTriple.addProperty("classification", strStockname);
                    importedTopTen.add(oSavingTriple);
                }
            }
            // only add classification if it has assignments; no assignments happen, if the ETF were added in previous runs and is written into the save file
            if (assignments.hasChildNodes()) {
                childrenNode.appendChild(classificationNodeForStock);
            }
        }
        logger.info(" - done!");

        return importedTopTen;
    }

    JsonArray importBranches(Document portfolioDocument, Security[] allSecurities, JsonArray cachedBranches, Element taxonomyElement) throws FileNotFoundException {
        logger.info("Importing branches...");
        NodeList allBranchesNodeList = taxonomyElement.getElementsByTagName("classification");

        JsonArray importedBranches = new JsonArray();
        Map<String, NodeRankTuple> oNodesWithNameAsKey = new HashMap<>();
        for (int indexBranch = 0; indexBranch < allBranchesNodeList.getLength(); indexBranch++) {
            Node branchNode = allBranchesNodeList.item(indexBranch);
            if (branchNode.getNodeType() == Node.ELEMENT_NODE) {
                String strNameOfNode = xmlHelper.getTextContent((Element) branchNode, "name");
                logger.info("Importing branch " + strNameOfNode);
                oNodesWithNameAsKey.put(strNameOfNode, new NodeRankTuple(branchNode, 0));
            }
        }

        for (int indexEtf = 0; indexEtf < allSecurities.length; indexEtf++) {
            Security security = allSecurities[indexEtf];
            if (security == null) {
                logger.warning("Security from portfolio is null?!?");
                continue;
            }
            logger.info("Security: " + security.getIsin());
            String[] oArrayOfBranchNames = security.getAllBranches();
            if (oArrayOfBranchNames != null && oArrayOfBranchNames.length > 0) {

                StringBuilder strMatchingStringForFile = new StringBuilder();
                for (String oArrayOfBranchname : oArrayOfBranchNames) {
                    String strBestMatch = "";
                    int currentLowestDistance = 1000;
                    for (String strBranchname : oNodesWithNameAsKey.keySet()) {
                        int temp = levenshteinDistance(oArrayOfBranchname, strBranchname);
                        //System.out.printf("%s --levenshtein-- %s\n", oArrayOfBranchNames[branchNameIndex], temp);
                        if (temp < currentLowestDistance) {
                            strBestMatch = strBranchname;
                            currentLowestDistance = temp;
                        }
                    }
                    //System.out.printf("%s --- %s --- %s\n", oArrayOfBranchNames[branchNameIndex], strBestMatch, currentLowestDistance);

                    int nPercentage = (int) Math.ceil(security.getPercentageOfBranch(oArrayOfBranchname) * 100.0);

                    boolean fSkipCurrentAdding = false;
                    // checking if the triple was added in an earlier run of the tool (therefor skip it -> no double entry AND it may have been moved
                    for (int nIndexAddedBranches = 0; nIndexAddedBranches < cachedBranches.size(); nIndexAddedBranches++) {
                        JsonObject oTriple = cachedBranches.get(nIndexAddedBranches).getAsJsonObject();
                        if (oTriple.get("weight").getAsInt() == nPercentage && oTriple.get("isin").getAsString().equals(security.getIsin()) && oTriple.get("classification").getAsString().equals(strBestMatch)) {
                            fSkipCurrentAdding = true;
                            JsonObject oSavingTriple = new JsonObject();
                            oSavingTriple.addProperty("weight", nPercentage);
                            oSavingTriple.addProperty("isin", security.getIsin());
                            oSavingTriple.addProperty("classification", strBestMatch);
                            importedBranches.add(oSavingTriple);
                            break;
                        }
                    }

                    if (nPercentage > 0 && !fSkipCurrentAdding) {
                        //System.out.printf("branch: %s with more than 0.0 found in etf: %s\n", strBranch, security.getName());
                        Element assignment = portfolioDocument.createElement("assignment");
                        NodeRankTuple oTuple = oNodesWithNameAsKey.get(strBestMatch);
                        Node branchNode = oTuple.oNode;

                        NodeList oAllChildren = branchNode.getChildNodes();
                        Element assigments = portfolioDocument.createElement("assignments");
                        for (int nNodeIndex = 0; nNodeIndex < oAllChildren.getLength(); nNodeIndex++) {
                            if (oAllChildren.item(nNodeIndex).getNodeType() == Node.ELEMENT_NODE && oAllChildren.item(nNodeIndex).getNodeName().equals("assignments")) {
                                assigments = (Element) oAllChildren.item(nNodeIndex);
                            }
                        }

                        int nRootsteps = returnRootsteps(assigments) + 3;
                        //System.out.printf("nRootsteps: %s for branch: %s\n", nRootsteps, strBestMatch);
                        Element investmentVehicle = portfolioDocument.createElement("investmentVehicle");
                        investmentVehicle.setAttribute("class", "security");
                        StringBuilder strParentsToClient = new StringBuilder();
                        for (int steps = 0; steps < nRootsteps; steps++) {
                            strParentsToClient.append("../");
                        }
                        //System.out.printf("strParentsToClient: %s\n", strParentsToClient);
                        investmentVehicle.setAttribute("reference", strParentsToClient + "securities/security[" + (indexEtf + 1) + "]");

                        Element weight = portfolioDocument.createElement("weight");
                        weight.setTextContent(Integer.toString(nPercentage));

                        Element rank = portfolioDocument.createElement("rank");
                        oTuple.nRank++;
                        rank.setTextContent(Integer.toString(oTuple.nRank));

                        assignment.appendChild(investmentVehicle);
                        assignment.appendChild(weight);
                        assignment.appendChild(rank);

                        assigments.appendChild(assignment);
                        strMatchingStringForFile.append("-> Branche (ETF / Fond) \"").append(oArrayOfBranchname).append("\" mit ").append((double) nPercentage / 100.0).append("% der Branche (PP) \"").append(strBestMatch).append("\" zugeordnet.\n");

                        JsonObject oSavingTriple = new JsonObject();
                        oSavingTriple.addProperty("weight", nPercentage);
                        oSavingTriple.addProperty("isin", security.getIsin());
                        oSavingTriple.addProperty("classification", strBestMatch);
                        importedBranches.add(oSavingTriple);
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
        }
        logger.info(" - done!");

        return importedBranches;
    }

    JsonArray importRegions(Document portfolioDocument, Security[] allSecurities, JsonArray cachedCountries, Element taxonomyElement) {
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
                for (int indexEtf = 0; indexEtf < allSecurities.length; indexEtf++) {
                    if (allSecurities[indexEtf] != null) {
                        int nPercentage = (int) Math.ceil(allSecurities[indexEtf].getPercentageOfCountry(strCountry) * 100.0);

                        boolean fSkipCurrentAdding = false;
                        // checking if the triple was added in an earlier run of the tool (therefor skip it -> no double entry AND it may have been moved
                        for (int nIndexAddedCountries = 0; nIndexAddedCountries < cachedCountries.size(); nIndexAddedCountries++) {
                            JsonObject oTriple = cachedCountries.get(nIndexAddedCountries).getAsJsonObject();
                            if (oTriple.get("weight").getAsInt() == nPercentage && oTriple.get("isin").getAsString().equals(allSecurities[indexEtf].getIsin()) && oTriple.get("classification").getAsString().equals(strCountry)) {
                                fSkipCurrentAdding = true;
                                JsonObject oSavingTriple = new JsonObject();
                                oSavingTriple.addProperty("weight", nPercentage);
                                oSavingTriple.addProperty("isin", allSecurities[indexEtf].getIsin());
                                oSavingTriple.addProperty("classification", strCountry);
                                importedRegions.add(oSavingTriple);
                                break;
                            }
                        }

                        if (nPercentage > 0 && !fSkipCurrentAdding) {
                            //System.out.printf("Country: %s with more than 0.0 found in etf: %s\n", strCountry, allSecurities[indexEtf].getName());
                            Element assignment = portfolioDocument.createElement("assignment");
                            NodeList oAllChildren = countryNode.getChildNodes();
                            Element assigments = portfolioDocument.createElement("assignments");
                            for (int nNodeIndex = 0; nNodeIndex < oAllChildren.getLength(); nNodeIndex++) {
                                if (oAllChildren.item(nNodeIndex).getNodeType() == Node.ELEMENT_NODE && oAllChildren.item(nNodeIndex).getNodeName().equals("assignments")) {
                                    assigments = (Element) oAllChildren.item(nNodeIndex);
                                }
                            }

                            int nRootsteps = returnRootsteps(assigments) + 3;
                            //System.out.printf("nRootsteps: %s \n", nRootsteps);
                            Element investmentVehicle = portfolioDocument.createElement("investmentVehicle");
                            investmentVehicle.setAttribute("class", "security");

                            StringBuilder strParentsToClient = new StringBuilder();
                            for (int steps = 0; steps < nRootsteps; steps++) {
                                strParentsToClient.append("../");
                            }
                            investmentVehicle.setAttribute("reference", strParentsToClient + "securities/security[" + (indexEtf + 1) + "]");

                            Element weight = portfolioDocument.createElement("weight");
                            weight.setTextContent(Integer.toString(nPercentage));

                            Element rank = portfolioDocument.createElement("rank");
                            nCountryAppearence++;
                            rank.setTextContent(Integer.toString(nCountryAppearence));

                            assignment.appendChild(investmentVehicle);
                            assignment.appendChild(weight);
                            assignment.appendChild(rank);

                            assigments.appendChild(assignment);

                            JsonObject oSavingTriple = new JsonObject();
                            oSavingTriple.addProperty("weight", nPercentage);
                            oSavingTriple.addProperty("isin", allSecurities[indexEtf].getIsin());
                            oSavingTriple.addProperty("classification", strCountry);
                            importedRegions.add(oSavingTriple);
                        }
                    }
                }
            }
        }
        logger.info(" - done!");

        return importedRegions;
    }

    private int returnRootsteps(Node node) {
        if (node.getParentNode().getNodeName().equals("client")) {
            return 0;
        } else {
            return 1 + returnRootsteps(node.getParentNode());
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
