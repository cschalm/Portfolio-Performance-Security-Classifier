package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import models.Security;
import models.SecurityDetailsCache;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xml.XmlHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static constants.PathConstants.LOGS_PATH;

public class PortfolioDocumentService {
    private static final Logger logger = Logger.getLogger(PortfolioDocumentService.class.getCanonicalName());
    // create random object - reuse this as often as possible
    Random random = new Random();
    XmlHelper xmlHelper = new XmlHelper();
    LevenshteinDistance distance = LevenshteinDistance.getDefaultInstance();

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
                        // if there is an entry in the cache-file, nothing is imported !!!
                        JsonArray importedRegions = importRegions(portfolioDocument, allSecurities, securityDetailsCache.getCachedCountries(), taxonomyElement);
                        securityDetailsCache.setCachedCountries(importedRegions);
                    }

                    if (taxonomyName.equals("Branchen (GICS)")) {
                        // if there is an entry in the cache-file, nothing is imported !!!
                        JsonArray importedBranches = importBranches(portfolioDocument, allSecurities, securityDetailsCache.getCachedBranches(), taxonomyElement);
                        securityDetailsCache.setCachedBranches(importedBranches);
                    }

                    if (taxonomyName.equals("Top Ten")) {
                        // if there is an entry in the cache-file, nothing is imported !!!
                        JsonArray importedTopTen = importTopTen(portfolioDocument, allSecurities, securityDetailsCache.getCachedTopTen(), taxonomyElement);
                        securityDetailsCache.setCachedTopTen(importedTopTen);
                    }
                }
            }
            // write all saved triples to avoid importing the same assignments several times for each run
            securityDetailsCache.save();
        } catch (IOException e) {
            logger.warning("Error updating XML: " + e.getMessage());
        }
    }

    JsonArray importTopTen(Document portfolioDocument, List<Security> allSecurities, JsonArray cachedTopTen, Element taxonomyElement) throws FileNotFoundException {
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
        for (Security security : allSecurities) {
            if (security != null) {
                StringBuilder holdingAssignmentLog = new StringBuilder();
                for (String holding : security.getHoldings().keySet()) {
                    int nPercentage = (int) Math.ceil(security.getPercentageOfHolding(holding) * 100.0);
                    if (nPercentage > 0)
                        holdingAssignmentLog.append("Holding \"").append(holding).append("\" mit ").append((double) nPercentage / 100.0).append("% zugeordnet.\n");
                }
                if (holdingAssignmentLog.length() > 0) {
                    File logsDir = new File(LOGS_PATH);
                    //noinspection ResultOfMethodCallIgnored
                    logsDir.mkdirs();
                    PrintWriter out = new PrintWriter(LOGS_PATH + security.getIsin() + "-holding.txt");
                    out.print(holdingAssignmentLog);
                    out.close();
                }
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
        Set<String> allStockNames = new LinkedHashSet<>();
        Set<String> fondStockNames = new HashSet<>();
        Set<String> amundiStockNames = new HashSet<>();
        for (Security security : allSecurities) {
            if (security != null) {
                if (!security.isFond()) {
                    if (security.getName() != null && !security.getName().isEmpty()) {
                        allStockNames.add(security.getName());
                    }
                } else {
                    Map<String, Double> holdings = security.getHoldings();
                    if (security.getName() != null && security.getName().toLowerCase().startsWith("amundi")) {
                        amundiStockNames.addAll(holdings.keySet());
                    } else {
                        fondStockNames.addAll(holdings.keySet());
                    }
                }
            }
        }
        allStockNames.addAll(fondStockNames);
        allStockNames.addAll(amundiStockNames);
        logger.fine("All Stocknames:");
        for (String name : allStockNames) {
            logger.fine(name);
        }
        return reduceSimilarStrings(allStockNames);
    }

    TreeMap<String, List<String>> reduceSimilarStrings(Collection<String> input) {
        TreeMap<String, List<String>> result = new TreeMap<>();
        for (String inputName : input) {
            if (result.isEmpty()) {
                // very first entry
                result.put(inputName, new ArrayList<>());
            } else {
                // check if similar name already exists in map
                boolean found = false;
                // Vanguard-ETFs should not be reduced as they most of the time only vary very little in their names
                if (!inputName.startsWith("Vanguard")) {
                    for (String existingName : result.keySet()) {
                        if (isNameSimilar(inputName, existingName)) {
                            // similar names exist and are not almost equal, add alternative name to list
                            result.get(existingName).add(inputName);
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    // maybe here we should check the spelling as the primary name later sets the name of the folder
                    result.put(inputName, new ArrayList<>());
                }
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Reduced Stocknames:");
            for (String name : result.keySet()) {
                logger.fine(name);
            }
        }
        return result;
    }

    boolean isNameSimilar(String one, String two) {
        final int LEVENSHTEINDISTANCELIMIT = 15;
        Integer levenshteinDistanceLowerCase = distance.apply(one.toLowerCase(), two.toLowerCase());
        logger.fine("Distance of \"" + one + "\" and \"" + two + "\": " + levenshteinDistanceLowerCase);
        if (levenshteinDistanceLowerCase == 0) {
            return true;
        }
        if (levenshteinDistanceLowerCase <= LEVENSHTEINDISTANCELIMIT) {
            // names are similar, less than LEVENSHTEINDISTANCELIMIT differences
            int indexOfDifferenceLowerCase = StringUtils.indexOfDifference(one.toLowerCase(), two.toLowerCase());
            int inputNameLength = one.length();
            int existingNameLength = two.length();
            int shorterNameLength = Math.min(inputNameLength, existingNameLength);
            logger.fine("IndexOfDifference of \"" + one + "\" and \"" + two + "\": " + indexOfDifferenceLowerCase + ", shorterNameLength: " + shorterNameLength);
            if (shorterNameLength <= indexOfDifferenceLowerCase) {
                // real subsets of names are identical, e.g. "SAP" and "SAP SE"
                return true;
            }
            // names differ in later characters, so the beginning of both names are equal
            return indexOfDifferenceLowerCase > 7;
        }
        return false;
    }

    JsonArray importBranches(Document portfolioDocument, List<Security> allSecurities, JsonArray cachedBranches, Element taxonomyElement) throws FileNotFoundException {
        logger.info("Importing branches...");
        NodeList allBranchesFromPortfolioNodeList = taxonomyElement.getElementsByTagName("classification");

        JsonArray importedBranches = new JsonArray();
        Map<String, PortfolioDocumentService.NodeRankTuple> branchNameFromPortfolioToNodeMap = new HashMap<>();
        for (int indexBranch = 0; indexBranch < allBranchesFromPortfolioNodeList.getLength(); indexBranch++) {
            Node branchFromPortfolioNode = allBranchesFromPortfolioNodeList.item(indexBranch);
            if (branchFromPortfolioNode.getNodeType() == Node.ELEMENT_NODE) {
                String branchNameFromPortfolio = xmlHelper.getTextContent((Element) branchFromPortfolioNode, "name");
                logger.fine("Importing branch " + branchNameFromPortfolio);
                branchNameFromPortfolioToNodeMap.put(branchNameFromPortfolio, new PortfolioDocumentService.NodeRankTuple(branchFromPortfolioNode, 0));
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

                StringBuilder branchAssignmentLog = new StringBuilder();
                for (String branchNameFromSecurity : branchNamesFromSecurity) {
                    String optimizedBranchNameFromSecurity = optimizeBranchNameFromSecurity(branchNameFromSecurity);
                    if ("DE0008402215".equalsIgnoreCase(security.getIsin())) {
                        // Hannover Rück is classified as "Versicherung" ?!?
                        optimizedBranchNameFromSecurity = "Rückversicherungen";
                    }
                    // skip not matching branches, e.g. "diverse Branchen"
                    if (optimizedBranchNameFromSecurity.isEmpty()) continue;
                    BestMatch bestMatch = getBestMatch(branchNameFromPortfolioToNodeMap.keySet(), optimizedBranchNameFromSecurity);

                    int nPercentage = (int) Math.ceil(security.getPercentageOfBranch(branchNameFromSecurity) * 100.0);

                    logger.fine("Branche \"" + branchNameFromSecurity + "\" mit " + ((double) nPercentage / 100.0) + "% der Branche (PP) \"" + bestMatch.bestMatchingBranchName + "\" zugeordnet. (Distanz: " + bestMatch.lowestDistance + ")");

                    boolean alreadyAddedBefore = isContainedInCache(cachedBranches, security.getIsin(), nPercentage, bestMatch.bestMatchingBranchName, importedBranches);

                    if (nPercentage > 0 && !alreadyAddedBefore) {
                        Element assignment = portfolioDocument.createElement("assignment");
                        PortfolioDocumentService.NodeRankTuple oTuple = branchNameFromPortfolioToNodeMap.get(bestMatch.bestMatchingBranchName);
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
                        branchAssignmentLog.append("Branche \"").append(branchNameFromSecurity).append("\" mit ").append((double) nPercentage / 100.0).append("% der Branche (PP) \"").append(bestMatch.bestMatchingBranchName).append("\" zugeordnet. Distanz: ").append(bestMatch.lowestDistance).append(")\n");

                        addNewJsonSecurity(importedBranches, bestMatch.bestMatchingBranchName, security.getIsin(), nPercentage);
                    }
                }
                if (branchAssignmentLog.length() > 0) {
                    File logsDir = new File(LOGS_PATH);
                    //noinspection ResultOfMethodCallIgnored
                    logsDir.mkdirs();
                    PrintWriter out = new PrintWriter(LOGS_PATH + security.getIsin() + "-branch.txt");
                    out.print(branchAssignmentLog);
                    out.close();
                }
            }
            indexEtf++;
        }
        logger.info(" - done!");

        return importedBranches;
    }

    BestMatch getBestMatch(Collection<String> branchNamesFromPortfolio, String branchNameFromSecurity) {
        String bestMatchingBranchName = "";
        int currentLowestDistance = 1000;
        for (String branchNameFromPortfolio : branchNamesFromPortfolio) {
            int temp = distance.apply(branchNameFromSecurity, branchNameFromPortfolio);

            if (temp < currentLowestDistance) {
                bestMatchingBranchName = branchNameFromPortfolio;
                currentLowestDistance = temp;
            }
        }
        return new BestMatch(bestMatchingBranchName, currentLowestDistance);
    }

    static class BestMatch {
        public final String bestMatchingBranchName;
        public final int lowestDistance;

        public BestMatch(String bestMatchingBranchName, int lowestDistance) {
            this.bestMatchingBranchName = bestMatchingBranchName;
            this.lowestDistance = lowestDistance;
        }
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
        Portfolio Performance (or GICS) and should be "optimized" - some are even misspelled
     */
    String optimizeBranchNameFromSecurity(String branchNameFromSecurity) {
        String result = branchNameFromSecurity;
        switch (branchNameFromSecurity) {
            case "IT/Telekommunikation":
                result = "Informationstechnologie";
                break;
            case "Telekomdienste":
            case "Telekommunikation":
                result = "Telekommunikationsdienste";
                break;
            case "diverse Branchen":
            case "Sonstige Branchen":
                result = "";
                break;
            case "Konsumgüter":
                result = "Basiskonsumgüter";
                break;
            case "Konsumgüter zyklisch":
                result = "Nicht-Basiskonsumgüter";
                break;
            case "Rohstoffe":
                result = "Roh-, Hilfs- & Betriebsstoffe";
                break;
            case "Computerherstellung":
            case "Hardware- Technologie, Speicherung und Peripheriegeräte":
                result = "Hardware Technologie, Speicherung & Peripherie";
                break;
            case "Fahrzeugbau":
                result = "Automobilbranche";
                break;
            case "Halbleiterelektronik":
                result = "Halbleiter";
                break;
            case "Halbleiter Ausstattung":
                result = "Geräte zur Halbleiterproduktion";
                break;
            case "Baumaterialien/Baukomponenten":
                result = "Baumaterialien";
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
            case "Versorger":
                result = "Versorgungsbetriebe";
                break;
            case "Bauwesen":
                result = "Bau- & Ingenieurswesen";
                break;
            case "Unterhaltungselektronik":
                result = "Verbraucherelektronik";
                break;
            case "Finanzdienstleistungen":
                result = "Private Finanzdienste";
                break;
            case "Finanzen":
                result = "Finanzwesen";
                break;
            case "Einzelhandel REITs":
                result = "Handels-REITs";
                break;
            case "Diversifizierte REITs":
                result = "Verschiedene REITs";
                break;
            case "Hypotheken-Immobilien-fonds (REITs)":
                result = "Hypotheken-, Immobilien-, Investment-, Trusts (REITs)";
                break;
            case "Elektrokomponenten":
                result = "Elektronische Komponenten";
                break;
            case "Elektrokomponenten & -geräte":
                result = "Elektronische Geräte & Instrumente";
                break;
            case "Industriemaschinenbau":
                result = "Industriemaschinen";
                break;
            default:
                break;
        }
        return result;
    }

    JsonArray importRegions(Document portfolioDocument, List<Security> allSecurities, JsonArray cachedCountries, Element taxonomyElement) throws FileNotFoundException {
        logger.info("Importing regions...");
        NodeList oListOfAllCountriesFromPortfolio = taxonomyElement.getElementsByTagName("classification");

        JsonArray importedRegions = new JsonArray();
        for (int indexCountry = 0; indexCountry < oListOfAllCountriesFromPortfolio.getLength(); indexCountry++) {
            Node countryNodeFromPortfolio = oListOfAllCountriesFromPortfolio.item(indexCountry);
            int nCountryAppearence = 0;
            if (countryNodeFromPortfolio.getNodeType() == Node.ELEMENT_NODE) {
                String strCountryFromPortfolio = xmlHelper.getTextContent((Element) countryNodeFromPortfolio, "name");
                if (strCountryFromPortfolio.equals("Vereinigte Staaten")) {
                    strCountryFromPortfolio = "USA";
                }
                logger.fine("CountryFromPortfolio " + strCountryFromPortfolio);
                int indexEtf = 0;
                for (Security security : allSecurities) {
                    if (security != null) {
                        // potentielles Umlautproblem!!!
                        int nPercentage = (int) Math.ceil(security.getPercentageOfCountry(strCountryFromPortfolio) * 100.0);

                        // cachedCountries is always empty in unit-test
                        boolean alreadyAddedBefore = isContainedInCache(cachedCountries, security.getIsin(), nPercentage, strCountryFromPortfolio, importedRegions);

                        if (nPercentage > 0 && !alreadyAddedBefore) {
                            Element assignments = portfolioDocument.createElement("assignments");
                            Element investmentVehicle = portfolioDocument.createElement("investmentVehicle");

                            assignments = linkAssignmentsToInvestmentVehicle(countryNodeFromPortfolio, assignments, investmentVehicle, indexEtf);

                            createAssignmentAndJson(portfolioDocument, strCountryFromPortfolio, security.getIsin(), nCountryAppearence, assignments, importedRegions, nPercentage, investmentVehicle);
                            nCountryAppearence++;
                        } else {
                            logger.fine("Skipping CountryFromPortfolio " + strCountryFromPortfolio + " as percentage is " + nPercentage);
                        }
                    }
                    indexEtf++;
                }
            }
        }
        for (Security security : allSecurities) {
            if (security != null) {
                StringBuilder countryAssignmentLog = new StringBuilder();
                for (String country : security.getCountries().keySet()) {
                    int nPercentage = (int) Math.ceil(security.getPercentageOfCountry(country) * 100.0);
                    if (nPercentage > 0)
                        countryAssignmentLog.append("Country \"").append(country).append("\" mit ").append((double) nPercentage / 100.0).append("% zugeordnet.\n");
                }
                if (countryAssignmentLog.length() > 0) {
                    File logsDir = new File(LOGS_PATH);
                    //noinspection ResultOfMethodCallIgnored
                    logsDir.mkdirs();
                    PrintWriter out = new PrintWriter(LOGS_PATH + security.getIsin() + "-country.txt");
                    out.print(countryAssignmentLog);
                    out.close();
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

    public static class NodeRankTuple {
        Node oNode;
        int nRank;

        public NodeRankTuple(Node node, int b) {
            this.oNode = node;
            this.nRank = b;
        }
    }

}
