package services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * One object of this class represents a security (share or ETF) containing data from a provider on the internet.
 * It contains meta-data for this share like the name and percentages for each country, branch and holding.
 */
public class SecurityDetails {
    public static final String ONVISTA_DETAILS_REQUEST_URL = "https://www.onvista.de/etf/anlageschwerpunkt/";
    public static final String ONVISTA_URL = "https://www.onvista.de";
    private static final Logger logger = Logger.getLogger(SecurityDetails.class.getCanonicalName());
    private String detailsRequestPath;
    private JsonObject rootNode;
    private final String isin;
    private String name;
    private String industry;
    private String country;
    private SecurityType securityType;

    public SecurityDetails(String cachePath, String isin) throws IOException, InterruptedException {
        this.isin = isin;
        initializeCache(cachePath);
        initializeSecurityType(cachePath, isin);
        initializeDetailsRequestUrl(cachePath, isin);
        initializeJsonRootNode(cachePath, isin);
        initializeMetaData(cachePath, isin);
    }

    private void initializeMetaData(String cachePath, String isin) {
        if (isETF() || isFonds()) {
            industry = "";
            country = "";
            name = "";
        } else {
            File industryAndNameCacheFileName = new File(cachePath, isin + "-metadata.txt");
            try (Stream<String> lines = Files.lines(Paths.get(industryAndNameCacheFileName.toURI()), StandardCharsets.UTF_8)) {
                List<String> input = lines.collect(Collectors.toList());
                if (!input.isEmpty()) {
                    industry = input.get(0);
                    country = input.get(1);
                    name = input.get(2);
                }
            } catch (IOException e) {
                logger.info("Branch for " + isin + " not found in cache, loading...");
                loadSecurityMetaData();
                try (PrintWriter savingImport = new PrintWriter(industryAndNameCacheFileName, StandardCharsets.UTF_8)) {
                    savingImport.print(industry + "\n");
                    savingImport.print(country + "\n");
                    savingImport.print(name + "\n");
                } catch (IOException fnfe) {
                    logger.warning("Error writing branch for " + isin + ": " + fnfe.getMessage());
                }
            }
        }
    }

    private void initializeJsonRootNode(String cachePath, String isin) throws IOException, InterruptedException {
        File jsonUrl = new File(cachePath, isin + ".json");
        try {
            rootNode = JsonParser.parseReader(new FileReader(jsonUrl, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            logger.info("JSON for " + isin + " not found in cache, loading...");
            String jsonResponsePart;
            if (isETF() || isFonds()) {
                String htmlPageAnlageschwerpunkt = readStringFromURL(ONVISTA_URL + detailsRequestPath);
                jsonResponsePart = extractJsonPartFromHtml(htmlPageAnlageschwerpunkt);
            } else {
                HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://www.onvista.de/suche.html?SEARCH_VALUE=" + isin + "&SELECTED_TOOL=ALL_TOOLS")).build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                jsonResponsePart = extractJsonPartFromHtml(response.body());
            }
            rootNode = JsonParser.parseString(jsonResponsePart).getAsJsonObject();
            try (PrintWriter savingImport = new PrintWriter(jsonUrl, StandardCharsets.UTF_8)) {
                savingImport.print(rootNode + "\n");
            } catch (IOException fnfe) {
                logger.warning("Error writing JSON for " + isin + ": " + fnfe.getMessage());
            }
        }
    }

    private void initializeDetailsRequestUrl(String cachePath, String isin) {
        File requestUrl = new File(cachePath, isin + ".txt");
        try (Stream<String> lines = Files.lines(Paths.get(requestUrl.toURI()), StandardCharsets.UTF_8)) {
            List<String> input = lines.collect(Collectors.toList());
            if (!input.isEmpty()) detailsRequestPath = input.get(0);
        } catch (IOException e) {
            logger.info("DetailsRequestPath for " + isin + " not found in cache, loading...");
            detailsRequestPath = readStringFromURL(ONVISTA_DETAILS_REQUEST_URL + isin);
            try (PrintWriter savingImport = new PrintWriter(requestUrl, StandardCharsets.UTF_8)) {
                savingImport.print(detailsRequestPath + "\n");
            } catch (IOException fnfe) {
                logger.warning("Error writing DetailsRequestPath for " + isin + ": " + fnfe.getMessage());
            }
        }
    }

    private void initializeCache(String cachePath) {
        File cacheDir = new File(cachePath);
        //noinspection ResultOfMethodCallIgnored
        cacheDir.mkdirs();
    }

    String readStringFromURL(String requestURL) {
        Scanner scanner = null;
        String result = "";
        try {
            scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8);
            scanner.useDelimiter("\\A");
            result = scanner.hasNext() ? scanner.next() : "";
        } catch (Exception e) {
            logger.warning("Error reading URL \"" + requestURL + "\": " + e.getMessage());
        } finally {
            assert scanner != null;
            scanner.close();
        }
        return result;
    }

    public JsonObject getBreakDownForSecurity() {
        return rootNode.getAsJsonObject("props")
                .getAsJsonObject("pageProps")
                .getAsJsonObject("data")
                .getAsJsonObject("breakdowns");
    }

    public String getCountryForSecurity() {
        String country = rootNode.getAsJsonObject("props")
                .getAsJsonObject("pageProps")
                .getAsJsonObject("data")
                .getAsJsonObject("snapshot")
                .getAsJsonObject("company")
                .get("nameCountry").getAsString();
        if (country == null || country.isEmpty()) {
            if (isin != null && isin.toUpperCase().startsWith("US")) country = "Vereinigte Staaten";
        }
        if ("Vereinigte Staaten".equalsIgnoreCase(country)) country = "USA";

        return country;
    }

    String extractJsonPartFromHtml(String htmlPageAnlageschwerpunkt) {
        String[] splitResponse = htmlPageAnlageschwerpunkt.split("<script id=\"__NEXT_DATA__\" type=\"application/json\">");
        return splitResponse[1].split("</script>")[0];
    }

    public boolean isETF() {
        return SecurityType.ETF.equals(securityType);
    }

    public boolean isFonds() {
        return SecurityType.FONDS.equals(securityType);
    }

    JsonObject getRootNode() {
        return rootNode;
    }

    void loadSecurityMetaData() {
        try {
            String url = "https://app.parqet.com/wertpapiere/" + isin;
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.selectXpath("//td[contains(text(), \"Industrie\")]");
            Element td = elements.get(0);
            List<TextNode> textNodes = td.selectXpath("..//td[2]/div/span/text()", TextNode.class);
            String industry = textNodes.get(0).text().trim();
            logger.fine("found industry \"" + industry + "\" for " + isin);
            this.industry = industry;

            elements = doc.selectXpath("//td[contains(text(), \"Name\")]");
            td = elements.get(0);
            textNodes = td.selectXpath("..//td[2]/div/text()", TextNode.class);
            String name = textNodes.get(0).text().trim();
            logger.fine("found name \"" + name + "\" for " + isin);
            this.name = name;

            elements = doc.selectXpath("//td[contains(text(), \"Sitz\")]");
            td = elements.get(0);
            textNodes = td.selectXpath("..//td[2]/div/div/span/text()", TextNode.class);
            String country = textNodes.get(0).text().trim();
            logger.fine("found country \"" + country + "\" for " + isin);
            this.country = country;
        } catch (IOException e) {
            logger.warning("Error loading branch for " + isin + ": " + e.getMessage());
        }
    }

    public String getIndustry() {
        return industry;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    enum SecurityType {
        SHARE,
        ETF,
        FONDS
    }

    SecurityType loadSecurityType() throws IOException, InterruptedException {
        String url = "https://app.parqet.com/wertpapiere/" + isin;
        HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.uri().getPath().startsWith("/etf/")) {
            return SecurityType.ETF;
        }
        if (response.uri().getPath().startsWith("/fonds/")) {
            return SecurityType.FONDS;
        }
        return SecurityType.SHARE;
    }

    void initializeSecurityType(String cachePath, String isin) throws IOException, InterruptedException {
        File securityTypeCacheFileName = new File(cachePath, isin + "-type.txt");
        try (Stream<String> lines = Files.lines(Paths.get(securityTypeCacheFileName.toURI()), StandardCharsets.UTF_8)) {
            List<String> input = lines.collect(Collectors.toList());
            if (!input.isEmpty()) {
                String type = input.get(0);
                securityType = SecurityType.valueOf(type);
            }
        } catch (IOException e) {
            logger.info("SecurityType for " + isin + " not found in cache, loading...");
            securityType = loadSecurityType();
            try (PrintWriter savingCache = new PrintWriter(securityTypeCacheFileName, StandardCharsets.UTF_8)) {
                savingCache.print(securityType + "\n");
            } catch (IOException fnfe) {
                logger.warning("Error writing SecurityType for " + isin + ": " + fnfe.getMessage());
            }
        }

    }
}
