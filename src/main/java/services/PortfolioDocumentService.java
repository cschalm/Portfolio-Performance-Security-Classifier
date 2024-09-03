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
import java.nio.file.FileSystems;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static constants.PathConstants.CACHE_PATH;

public class PortfolioDocumentService {
    private static final Logger logger = Logger.getLogger(PortfolioDocumentService.class.getCanonicalName());
    Random random = new Random();
    XmlHelper xmlHelper = new XmlHelper();
    LevenshteinDistance distance = LevenshteinDistance.getDefaultInstance();
    private String logsPath = CACHE_PATH;

    public PortfolioDocumentService() {
    }

    public PortfolioDocumentService(String logsPath) {
        this.logsPath = logsPath;
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
                        // if there is an entry in the cache-file, nothing is imported !!!
                        JsonArray importedRegions = importRegions(portfolioDocument, allSecurities, taxonomyElement);
                        securityDetailsCache.setCachedCountries(importedRegions);
                    }

                    if (taxonomyName.equals("Branchen (GICS)")) {
                        // if there is an entry in the cache-file, nothing is imported !!!
                        JsonArray importedIndustries = importIndustries(portfolioDocument, allSecurities, taxonomyElement);
                        securityDetailsCache.setCachedIndustries(importedIndustries);
                    }

                    if (taxonomyName.equals("Unternehmensgewichtung")) {
                        // if there is an entry in the cache-file, nothing is imported !!!
                        JsonArray importedTopTen = importCompanyRatio(portfolioDocument, allSecurities, taxonomyElement);
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

    JsonArray importCompanyRatio(Document portfolioDocument, List<Security> allSecurities, Element taxonomyElement) throws FileNotFoundException {
        logger.info("Importing Unternehmensgewichtung..");
        JsonArray importedTopTen = new JsonArray();

        TreeMap<String, List<String>> allStockNames = collectAllStockNames(allSecurities);

        // search for "children" element as direct child of "root"
        Node rootOfTopTenNode = taxonomyElement.getElementsByTagName("root").item(0);
        Element childrenElement = xmlHelper.getFirstChildElementWithNodeName(rootOfTopTenNode, "children");
        if (childrenElement == null) {
            childrenElement = portfolioDocument.createElement("children");
        } else {
            // remove orphan Unternehmensgewichtung assignments
            NodeList allTopTenFromPortfolioNodeList = taxonomyElement.getElementsByTagName("classification");
            for (int indexTopTen = 0; indexTopTen < allTopTenFromPortfolioNodeList.getLength(); indexTopTen++) {
                Node topTenFromPortfolioNode = allTopTenFromPortfolioNodeList.item(indexTopTen);
                if (topTenFromPortfolioNode.getNodeType() == Node.ELEMENT_NODE) {
                    String topTenNameFromPortfolio = xmlHelper.getTextContent((Element) topTenFromPortfolioNode, "name");
                    // check for each assignment if the corresponding stock still exists in portfolio
                    NodeList assignments = ((Element) topTenFromPortfolioNode).getElementsByTagName("assignment");
                    int indexSecurityToCheck;
                    for (int indexAssignments = 0; indexAssignments < assignments.getLength(); indexAssignments++) {
                        Node assignment = assignments.item(indexAssignments);
                        if (assignment.getNodeType() == Node.ELEMENT_NODE) {
                            Element investmentVehicle = xmlHelper.getFirstChildElementWithNodeName(assignment, "investmentVehicle");
                            if (investmentVehicle == null || !investmentVehicle.getAttribute("class").equals("security"))
                                continue;
                            String reference = investmentVehicle.getAttribute("reference");
                            if (reference.isEmpty()) continue;
                            if (reference.endsWith("securities/security")) {
                                indexSecurityToCheck = 0;
                            } else {
                                String foundIndex = reference.substring(reference.indexOf('[') + 1, reference.indexOf(']'));
                                indexSecurityToCheck = Integer.parseInt(foundIndex) - 1;
                            }
                            if (indexSecurityToCheck < 0 || indexSecurityToCheck >= allSecurities.size()) continue;
                            int finalIndexSecurityToCheck = indexSecurityToCheck;
                            Optional<Security> security = allSecurities.stream().filter(s -> s.getIndexInPortfolio() == finalIndexSecurityToCheck).findFirst();
                            if (security.isPresent() && !hasSecurityHolding(security.get(), allStockNames, topTenNameFromPortfolio)) {
                                logger.fine("Removing " + security.get() + " from Unternehmensgewichtung " + topTenNameFromPortfolio);
                                Node assignmentsNode = assignment.getParentNode();
                                assignmentsNode.removeChild(assignment);
                                if (((Element) assignmentsNode).getElementsByTagName("assignment").getLength() == 0) {
                                    // no assignments left, so we remove this classification
                                    logger.fine("Removing Unternehmensgewichtung " + topTenNameFromPortfolio);
                                    topTenFromPortfolioNode.getParentNode().removeChild(topTenFromPortfolioNode);
                                }
                            }
                        }
                    }
                }
            }
        }
        // add or update Unternehmensgewichtung
        for (String stockName : allStockNames.keySet()) {
            logger.fine("Stockname: " + stockName);

            Element existingClassification = findClassificationBySimilarName(childrenElement, stockName);
            List<Security> existingSecurities = findSecuritiesByHolding(allSecurities, allStockNames, stockName);
            if (existingClassification != null && !existingSecurities.isEmpty()) {
                for (Security existingSecurity : existingSecurities) {
                    int indexOfExistingSecurity = existingSecurity.getIndexInPortfolio();
                    List<Element> existingAssignmentsList = findAssignmentsBySecurityIndex(existingClassification, indexOfExistingSecurity + 1);
                    if (!existingAssignmentsList.isEmpty()) {
                        // update existing or add additional assignments
                        List<Integer> allPercentagesOfHoldingsForSecurityByStockname = getAllPercentagesOfHoldingsForSecurityByStockname(existingSecurity, allStockNames, stockName);
                        for (int index = 0; index < allPercentagesOfHoldingsForSecurityByStockname.size(); index++) {
                            Integer percentage = allPercentagesOfHoldingsForSecurityByStockname.get(index);
                            if (existingAssignmentsList.size() > index) {
                                // update EXISTING assignment
                                Element existingAssignment = existingAssignmentsList.get(index);
                                logger.fine("Updating TopTen " + stockName + " with " + existingSecurity + ": " + percentage);
                                updateWeightOfAssignment(existingAssignment, Integer.toString(percentage));
                            } else {
                                // add NEW assignment
                                logger.fine("Adding " + existingSecurity + " to Unternehmensgewichtung " + stockName + ": " + percentage);
                                Element assignments = xmlHelper.getFirstChildElementWithNodeName(existingClassification, "assignments");
                                addTopTenAssignment(portfolioDocument, stockName, existingSecurity, 0, assignments, importedTopTen, percentage);
                            }
                        }
                    } else {
                        // add new assignment(s) to EXISTING classification!
                        Element assignments = xmlHelper.getFirstChildElementWithNodeName(existingClassification, "assignments");
                        if (assignments == null) {
                            assignments = portfolioDocument.createElement("assignments");
                            childrenElement.appendChild(assignments);
                        }
                        logger.fine("Adding " + existingSecurity + " to Unternehmensgewichtung for " + stockName);
                        addAssignmentToAssignments(portfolioDocument, existingSecurity, stockName, importedTopTen, allStockNames, assignments, 0);
                    }
                }
            } else {
                // add new assignment to NEW classification!
                logger.fine("Adding all holdings to Unternehmensgewichtung for " + stockName);
                Element assignments = portfolioDocument.createElement("assignments");
                childrenElement.appendChild(assignments);
                addAssignmentsToAssignments(portfolioDocument, allSecurities, stockName, importedTopTen, allStockNames, assignments);
                // only add classification if it has assignments; no assignments happen, if the ETF were added in previous runs and is written into the save file
                if (assignments.hasChildNodes()) {
                    Element classificationNodeForStock = createNewClassification(portfolioDocument, stockName, assignments);
                    childrenElement.appendChild(classificationNodeForStock);
                }
            }
        }
        // write logfile
        for (Security security : allSecurities) {
            StringBuilder holdingAssignmentLog = new StringBuilder();
            for (String holding : security.getHoldings().keySet()) {
                int nPercentage = getPercentageOfHolding(security, holding);
                if (nPercentage > 0)
                    holdingAssignmentLog.append("Holding \"").append(holding).append("\" assigned with ").append((double) nPercentage / 100.0).append("%\n");
            }
            writeLogfile4Security(security, holdingAssignmentLog, "holding");
        }
        logger.info(" - done!");

        return importedTopTen;
    }

    private Element createNewClassification(Document portfolioDocument, String stockName, Element assignments) {
        // setting each stock as own "classification"
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

        Element weight = portfolioDocument.createElement("weight");
        weight.setTextContent("10000");

        Element rankElement = portfolioDocument.createElement("rank");
        rankElement.setTextContent("0");

        classificationNodeForStock.appendChild(id);
        classificationNodeForStock.appendChild(name);
        classificationNodeForStock.appendChild(color);
        classificationNodeForStock.appendChild(parent);
        classificationNodeForStock.appendChild(children);
        classificationNodeForStock.appendChild(assignments);
        classificationNodeForStock.appendChild(weight);
        classificationNodeForStock.appendChild(rankElement);
        return classificationNodeForStock;
    }

    private void addAssignmentsToAssignments(Document portfolioDocument, List<Security> allSecurities, String stockName, JsonArray importedTopTen, TreeMap<String, List<String>> allStockNames, Element assignments) {
        int rank = 0;
        for (Security security : allSecurities) {
            rank = addAssignmentToAssignments(portfolioDocument, security, stockName, importedTopTen, allStockNames, assignments, rank);
        }
    }

    private int addAssignmentToAssignments(Document portfolioDocument, Security security, String stockName, JsonArray importedTopTen, TreeMap<String, List<String>> allStockNames, Element assignments, int rank) {
        // find security that contains the current stock identified by any similar name
        if (security.getHoldings().containsKey(stockName)) {
            // primary name
            rank = addTopTenAssignment(portfolioDocument, stockName, security, 0, assignments, importedTopTen);
        }
        // alternative names
        List<String> alternativeNames = allStockNames.get(stockName);
        for (String alternativeName : alternativeNames) {
            if (security.getHoldings().containsKey(alternativeName)) {
                rank = addTopTenAssignment(portfolioDocument, alternativeName, security, rank, assignments, importedTopTen);
            }
        }

        return rank;
    }

    private int addTopTenAssignment(Document portfolioDocument, String stockName, Security security, int rank, Element assignments, JsonArray importedTopTen) {
        int percentage = getPercentageOfHolding(security, stockName);

        return addTopTenAssignment(portfolioDocument, stockName, security, rank, assignments, importedTopTen, percentage);
    }

    private int addTopTenAssignment(Document portfolioDocument, String stockName, Security security, int rank, Element assignments, JsonArray importedTopTen, int percentage) {
        boolean found = false;
        // verify that this stock was not imported by an alternative name before
        for (int i = 0; i < importedTopTen.size(); i++) {
            JsonObject topTen = importedTopTen.get(i).getAsJsonObject();
            if (jsonObjectEqualsInWeightAndIsinAndClassification(topTen, security.getIsin(), percentage, stockName)) {
                found = true;
                break;
            }
        }
        if (!found) {
            Element investmentVehicle = portfolioDocument.createElement("investmentVehicle");
            investmentVehicle.setAttribute("class", "security");
            investmentVehicle.setAttribute("reference", "../../../../../../../../securities/security[" + (security.getIndexInPortfolio() + 1) + "]");

            Element assignment = createAssignmentElement(portfolioDocument, rank, percentage);
            assignment.appendChild(investmentVehicle);
            assignments.appendChild(assignment);
            JsonObject securityJsonObject = createSecurityJson(stockName, security.getIsin(), percentage);
            importedTopTen.add(securityJsonObject);
            rank++;
        }
        return rank;
    }

    private JsonObject createSecurityJson(String classification, String isin, int weight) {
        JsonObject security = new JsonObject();
        security.addProperty("weight", weight);
        security.addProperty("isin", isin);
        security.addProperty("classification", classification);

        return security;
    }

    private Element createAssignmentElement(Document doc, int rank, int weight) {
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
        if (levenshteinDistanceLowerCase <= 1) {
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
            if (one.toLowerCase().startsWith("alphabet") && two.toLowerCase().startsWith("alphabet")) {
                return true;
            }
            if (one.contains(" ") && two.contains(" ")) {
                // both names contain a blank and therefore consist of multiple parts
                int oneIndexOfBlank = one.indexOf(" ");
                int twoIndexOfBlank = two.indexOf(" ");
                if (oneIndexOfBlank + 1 == indexOfDifferenceLowerCase && twoIndexOfBlank + 1 == indexOfDifferenceLowerCase) {
                    String[] oneParts = one.toLowerCase().split("\\h");
                    String[] twoParts = two.toLowerCase().split("\\h");
                    return isNameSimilar(oneParts[1], twoParts[1]);
                }
            }
            return indexOfDifferenceLowerCase > 7;
        }
        return false;
    }

    JsonArray importIndustries(Document portfolioDocument, List<Security> allSecurities, Element taxonomyElement) throws FileNotFoundException {
        logger.info("Importing industries...");
        NodeList allIndustriesFromPortfolioNodeList = taxonomyElement.getElementsByTagName("classification");

        JsonArray importedIndustries = new JsonArray();
        Map<String, PortfolioDocumentService.NodeRankTuple> industryNameFromPortfolioToNodeMap = new HashMap<>();
        for (int indexIndustry = 0; indexIndustry < allIndustriesFromPortfolioNodeList.getLength(); indexIndustry++) {
            Node industryFromPortfolioNode = allIndustriesFromPortfolioNodeList.item(indexIndustry);
            if (industryFromPortfolioNode.getNodeType() == Node.ELEMENT_NODE) {
                String industryNameFromPortfolio = xmlHelper.getTextContent((Element) industryFromPortfolioNode, "name");
                logger.fine("Importing industry " + industryNameFromPortfolio);
                industryNameFromPortfolioToNodeMap.put(industryNameFromPortfolio, new PortfolioDocumentService.NodeRankTuple(industryFromPortfolioNode, 0));
                removeOrphanIndustryAssignment(industryFromPortfolioNode, industryNameFromPortfolio, allSecurities);
            }
        }
        for (Security security : allSecurities) {
            logger.fine("Security: " + security);

            StringBuilder industryAssignmentLog = new StringBuilder();
            for (String industryNameFromSecurity : security.getIndustries().keySet()) {
                String optimizedIndustryNameFromSecurity = optimizeIndustryNameFromSecurity(industryNameFromSecurity);
                if ("DE0008402215".equalsIgnoreCase(security.getIsin())) {
                    // Hannover Rück is classified as "Versicherung" ?!?
                    optimizedIndustryNameFromSecurity = "Rückversicherungen";
                }
                // skip not matching industries, e.g. "diverse Branchen"
                if (optimizedIndustryNameFromSecurity.isEmpty()) continue;
                BestMatch bestMatch = getBestMatch(industryNameFromPortfolioToNodeMap.keySet(), optimizedIndustryNameFromSecurity);

                int percentage = (int) Math.round(security.getPercentageOfBranch(industryNameFromSecurity) * 100.0);

                PortfolioDocumentService.NodeRankTuple oTuple = industryNameFromPortfolioToNodeMap.get(bestMatch.bestMatchingIndustryName);
                Node industryNode = oTuple.oNode;
                Element assignment = findAssignmentBySecurityIndex(industryNode, security.getIndexInPortfolio() + 1);
                if (percentage > 0) {
                    // check if assignment already exists and needs to be updated or added
                    if (assignment != null) {
                        // update
                        updateWeightOfAssignment(assignment, Integer.toString(percentage));
                    } else {
                        // create and add new assignment
                        assignment = createAssignmentElement(portfolioDocument, ++oTuple.nRank, percentage);
                        Element investmentVehicle = portfolioDocument.createElement("investmentVehicle");
                        Element assignments = linkAssignmentsToInvestmentVehicle(industryNode, investmentVehicle, security.getIndexInPortfolio());
                        assignment.appendChild(investmentVehicle);
                        assignments.appendChild(assignment);

                        JsonObject securityJsonObject = createSecurityJson(bestMatch.bestMatchingIndustryName, security.getIsin(), percentage);
                        importedIndustries.add(securityJsonObject);

                        industryAssignmentLog.append("Industry \"").append(industryNameFromSecurity).append("\" assigned with ").append((double) percentage / 100.0).append("% to industry in PP \"").append(bestMatch.bestMatchingIndustryName).append("\". LevenshteinDistance in naming: ").append(bestMatch.lowestDistance).append("\n");
                    }
                }
            }
            writeLogfile4Security(security, industryAssignmentLog, "industry");
        }
        logger.info(" - done!");

        return importedIndustries;
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
        public final String bestMatchingIndustryName;
        public final int lowestDistance;

        public BestMatch(String bestMatchingIndustryName, int lowestDistance) {
            this.bestMatchingIndustryName = bestMatchingIndustryName;
            this.lowestDistance = lowestDistance;
        }
    }

    private Element linkAssignmentsToInvestmentVehicle(Node node, Element investmentVehicle, int indexOfSecurity) {
        Element assignments = xmlHelper.getFirstChildElementWithNodeName(node, "assignments");

        assert assignments != null;
        int stepsToRoot = returnRootSteps(assignments) + 3;

        investmentVehicle.setAttribute("reference", "../".repeat(stepsToRoot) + "securities/security[" + (indexOfSecurity + 1) + "]");
        investmentVehicle.setAttribute("class", "security");

        return assignments;
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
    String optimizeIndustryNameFromSecurity(String industryNameFromSecurity) {
        String result = industryNameFromSecurity;
        switch (industryNameFromSecurity) {
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
            case "Kraftfahrzeuge":
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

    JsonArray importRegions(Document portfolioDocument, List<Security> allSecurities, Element taxonomyElement) throws FileNotFoundException {
        logger.info("Importing regions...");
        NodeList allCountriesFromPortfolioList = taxonomyElement.getElementsByTagName("classification");

        JsonArray importedRegions = new JsonArray();
        for (int indexCountry = 0; indexCountry < allCountriesFromPortfolioList.getLength(); indexCountry++) {
            Node countryFromPortfolioNode = allCountriesFromPortfolioList.item(indexCountry);
            int rank = 0;
            if (countryFromPortfolioNode.getNodeType() == Node.ELEMENT_NODE) {
                String countryNameFromPortfolio = xmlHelper.getTextContent((Element) countryFromPortfolioNode, "name");
                if (countryNameFromPortfolio.equals("Vereinigte Staaten")) {
                    countryNameFromPortfolio = "USA";
                }
                logger.fine("CountryFromPortfolio " + countryNameFromPortfolio);
                for (Security security : allSecurities) {
                    // potential problem with german umlauts due to different encodings!!!
                    int percentage = (int) Math.round(security.getPercentageOfCountry(countryNameFromPortfolio) * 100.0);

                    Element assignment = findAssignmentBySecurityIndex(countryFromPortfolioNode, security.getIndexInPortfolio() + 1);
                    if (percentage == 0) {
                        // maybe this holding was contained before, so check if we have to remove it from this country
                        if (assignment != null)
                            assignment.getParentNode().removeChild(assignment);
                    } else {
                        // check if assignment already exists and needs to be updated or added
                        if (assignment != null) {
                            // update
                            updateWeightOfAssignment(assignment, Integer.toString(percentage));
                        } else {
                            // create and add new assignment
                            assignment = createAssignmentElement(portfolioDocument, rank, percentage);

                            Element investmentVehicle = portfolioDocument.createElement("investmentVehicle");
                            Element assignments = linkAssignmentsToInvestmentVehicle(countryFromPortfolioNode, investmentVehicle, security.getIndexInPortfolio());
                            assignment.appendChild(investmentVehicle);
                            assignments.appendChild(assignment);

                            JsonObject securityJsonObject = createSecurityJson(countryNameFromPortfolio, security.getIsin(), percentage);
                            importedRegions.add(securityJsonObject);

                            rank++;
                        }
                    }
                }
            }
        }
        // write logfile
        for (Security security : allSecurities) {
            if (security != null) {
                StringBuilder countryAssignmentLog = new StringBuilder();
                for (String country : security.getCountries().keySet()) {
                    int nPercentage = (int) Math.round(security.getPercentageOfCountry(country) * 100.0);
                    if (nPercentage > 0)
                        countryAssignmentLog.append("Country \"").append(country).append("\" assigned with ").append((double) nPercentage / 100.0).append("%\n");
                }
                writeLogfile4Security(security, countryAssignmentLog, "country");
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

    void writeLogfile4Security(Security security, StringBuilder logLines, String fileSuffix) throws FileNotFoundException {
        if (logLines.length() > 0) {
            File logsDir = new File(logsPath);
            //noinspection ResultOfMethodCallIgnored
            logsDir.mkdirs();
            PrintWriter out = new PrintWriter(logsPath + FileSystems.getDefault().getSeparator() + security.getIsin() + "-" + fileSuffix + ".txt");
            out.print(logLines);
            out.close();
        }
    }

    Element findAssignmentBySecurityIndex(Node parent, int securityIndex) {
        Element assignments = xmlHelper.getFirstChildElementWithNodeName(parent, "assignments");
        if (assignments == null) return null;
        NodeList children = assignments.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE && children.item(i).getNodeName().equals("assignment")) {
                Element assignment = (Element) children.item(i);
                String reference = getReference(assignment);
                if (reference == null || reference.isEmpty()) continue;
                if (securityIndex == 1 && reference.endsWith("securities/security")) return assignment;
                if (!(reference.contains("[") && reference.contains("]"))) continue;
                String foundIndex = reference.substring(reference.indexOf('[') + 1, reference.indexOf(']'));
                if (Integer.parseInt(foundIndex) == securityIndex) return assignment;
            }
        }
        return null;
    }

    private String getReference(Element assignment) {
        Element investmentVehicle = xmlHelper.getFirstChildElementWithNodeName(assignment, "investmentVehicle");
        if (investmentVehicle == null || !investmentVehicle.getAttribute("class").equals("security")) return null;
        String reference = investmentVehicle.getAttribute("reference");
        return reference;
    }

    List<Element> findAssignmentsBySecurityIndex(Node parent, int securityIndex) {
        List<Element> foundAssignments = new ArrayList<>();
        Element assignments = xmlHelper.getFirstChildElementWithNodeName(parent, "assignments");
        if (assignments == null) return foundAssignments;
        NodeList children = assignments.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE && children.item(i).getNodeName().equals("assignment")) {
                Element assignment = (Element) children.item(i);
                String reference = getReference(assignment);
                if (reference == null || reference.isEmpty()) continue;
                if (securityIndex == 1 && reference.endsWith("securities/security")) {
                    foundAssignments.add(assignment);
                    continue;
                }
                if (!(reference.contains("[") && reference.contains("]"))) continue;
                String foundIndex = reference.substring(reference.indexOf('[') + 1, reference.indexOf(']'));
                if (Integer.parseInt(foundIndex) == securityIndex) foundAssignments.add(assignment);
            }
        }
        return foundAssignments;
    }

    Element updateWeightOfAssignment(Element assignment, String weight) {
        Node weightNode = xmlHelper.getFirstChild(assignment, "weight");
        weightNode.setTextContent(weight);

        return assignment;
    }

    void removeOrphanIndustryAssignment(Node industryFromPortfolio, String industryNameFromPortfolio, List<Security> allSecurities) {
        for (Security security : allSecurities) {
            logger.fine("Security: " + security);
            Element assignment = findAssignmentBySecurityIndex(industryFromPortfolio, security.getIndexInPortfolio() + 1);
            if (assignment != null) {
                // there is an assignment from this security to the current industry-node, but should it be removed?
                boolean found = false;
                for (String industryNameFromSecurity : security.getIndustries().keySet()) {
                    String optimizedIndustryNameFromSecurity = optimizeIndustryNameFromSecurity(industryNameFromSecurity);
                    if ("DE0008402215".equalsIgnoreCase(security.getIsin())) {
                        // Hannover Rück is classified as "Versicherung" ?!?
                        optimizedIndustryNameFromSecurity = "Rückversicherungen";
                    }
                    // skip not matching industries, e.g. "diverse Branchen"
                    if (optimizedIndustryNameFromSecurity.isEmpty()) continue;

                    if (isNameSimilar(industryNameFromPortfolio, optimizedIndustryNameFromSecurity)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // maybe this holding was contained before, but now it isn't
                    assignment.getParentNode().removeChild(assignment);
                }
            }
        }
    }

    Element findClassificationByName(Element parent, String name) {
        NodeList allClassificationsFromPortfolioList = parent.getElementsByTagName("classification");
        for (int indexClassification = 0; indexClassification < allClassificationsFromPortfolioList.getLength(); indexClassification++) {
            Node classificationFromPortfolioNode = allClassificationsFromPortfolioList.item(indexClassification);
            if (classificationFromPortfolioNode.getNodeType() == Node.ELEMENT_NODE) {
                String classificationNameFromPortfolio = xmlHelper.getTextContent((Element) classificationFromPortfolioNode, "name");
                // adjust name for country "USA"
                if (classificationNameFromPortfolio.equals("Vereinigte Staaten")) {
                    classificationNameFromPortfolio = "USA";
                }
                if (name.equals(classificationNameFromPortfolio)) return (Element) classificationFromPortfolioNode;
            }
        }
        return null;
    }

    Element findClassificationBySimilarName(Element parent, String name) {
        NodeList allClassificationsFromPortfolioList = parent.getElementsByTagName("classification");
        for (int indexClassification = 0; indexClassification < allClassificationsFromPortfolioList.getLength(); indexClassification++) {
            Node classificationFromPortfolioNode = allClassificationsFromPortfolioList.item(indexClassification);
            if (classificationFromPortfolioNode.getNodeType() == Node.ELEMENT_NODE) {
                String classificationNameFromPortfolio = xmlHelper.getTextContent((Element) classificationFromPortfolioNode, "name");
                // adjust name for country "USA"
                if (classificationNameFromPortfolio.equals("Vereinigte Staaten")) {
                    classificationNameFromPortfolio = "USA";
                }
                if (isNameSimilar(name, classificationNameFromPortfolio))
                    return (Element) classificationFromPortfolioNode;
            }
        }
        return null;
    }

    List<Security> findSecuritiesByHolding(List<Security> allSecurities, TreeMap<String, List<String>> allStockNames, String holdingName) {
        Set<Security> foundSecurities = new HashSet<>();
        for (Security security : allSecurities) {
            // find security that contains the current stock identified by any similar name
            if (security.getHoldings().containsKey(holdingName)) {
                // primary name
                foundSecurities.add(security);
            }
            // alternative names
            List<String> alternativeNames = allStockNames.get(holdingName);
            if (alternativeNames != null) {
                for (String alternativeName : alternativeNames) {
                    if (security.getHoldings().containsKey(alternativeName)) {
                        foundSecurities.add(security);
                    }
                }
            }
        }
        return new ArrayList<>(foundSecurities);
    }

    boolean hasSecurityHolding(Security security, TreeMap<String, List<String>> allStockNames, String holdingName) {
        if (security != null) {
            // find security that contains the current stock identified by any similar name
            if (security.getHoldings().containsKey(holdingName)) {
                // primary name
                return true;
            } else {
                // alternative names
                List<String> alternativeNames = allStockNames.get(holdingName);
                if (alternativeNames != null) {
                    for (String alternativeName : alternativeNames) {
                        if (security.getHoldings().containsKey(alternativeName)) {
                            return true;
                        }
                    }
                }
            }
            // maybe name is written only similar?
            for (String existingName : security.getHoldings().keySet()) {
                if (isNameSimilar(holdingName, existingName)) return true;
            }
        }
        return false;
    }

    List<Integer> getAllPercentagesOfHoldingsForSecurityByStockname(Security security, TreeMap<String, List<String>> allStockNames, String holdingName) {
        List<Integer> percentages = new ArrayList<>();
        if (security != null) {
            // find security that contains the current stock identified by any similar name
            if (security.getHoldings().containsKey(holdingName)) {
                // primary name
                percentages.add(getPercentageOfHolding(security, holdingName));
            }
            // alternative names
            List<String> alternativeNames = allStockNames.get(holdingName);
            if (alternativeNames != null) {
                for (String alternativeName : alternativeNames) {
                    if (security.getHoldings().containsKey(alternativeName)) {
                        percentages.add(getPercentageOfHolding(security, alternativeName));
                    }
                }
            }
        }
        return percentages;
    }

    int getPercentageOfHolding(Security security, String holdingName) {
        return (int) Math.round(security.getPercentageOfHolding(holdingName) * 100.0);
    }

}
