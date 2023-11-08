package services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static constants.PathConstants.LOGS_PATH;

public class SecurityDetails {
    public static final String ONVISTA_DETAILS_REQUEST_URL = "https://www.onvista.de/etf/anlageschwerpunkt/";
    public static final String ONVISTA_URL = "https://www.onvista.de";
    private static final Logger logger = Logger.getLogger(SecurityDetails.class.getCanonicalName());
    private String detailsRequestPath;
    private JsonObject rootNode;

    public SecurityDetails(String isin) {
        File requestUrl = new File(LOGS_PATH + isin + ".txt");
        try (Stream<String> lines = Files.lines(Paths.get(LOGS_PATH + isin + ".txt"))) {
            List<String> input = lines.collect(Collectors.toList());
            if (!input.isEmpty()) detailsRequestPath = input.get(0);
        } catch (IOException e) {
            logger.info("DetailsRequestPath for " + isin + " not found in cache, loading...");
            detailsRequestPath = readStringFromURL(ONVISTA_DETAILS_REQUEST_URL + isin);
            try (PrintWriter savingImport = new PrintWriter(requestUrl)) {
                savingImport.print(detailsRequestPath + "\n");
            } catch (FileNotFoundException fnfe) {
                logger.warning("Error writing DetailsRequestPath for " + isin + ": " + fnfe.getMessage());
            }
        }
        File jsonUrl = new File(LOGS_PATH + isin + ".json");
        try {
            // if there is an entry in the cache-file, nothing is imported !!!
            rootNode = JsonParser.parseReader(new FileReader(jsonUrl)).getAsJsonObject();
        } catch (Exception e) {
            logger.warning("JSON for " + isin + " not found in cache, loading...");
            String htmlPageAnlageschwerpunkt = readStringFromURL(ONVISTA_URL + detailsRequestPath);
            String jsonResponsePart = extractJsonPartFromHtml(htmlPageAnlageschwerpunkt);
            rootNode = JsonParser.parseString(jsonResponsePart).getAsJsonObject();
            try (PrintWriter savingImport = new PrintWriter(jsonUrl)) {
                savingImport.print(rootNode + "\n");
            } catch (FileNotFoundException fnfe) {
                logger.warning("Error writing JSON for " + isin + ": " + fnfe.getMessage());
            }
        }
    }

    public String getDetailsRequestPath() {
        return detailsRequestPath;
    }

    String readStringFromURL(String requestURL) {
        Scanner scanner = null;
        String result = "";
        try {
            scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString());
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
        return rootNode.getAsJsonObject("props").getAsJsonObject("pageProps").getAsJsonObject("data").getAsJsonObject("breakdowns");
    }

    private String extractJsonPartFromHtml(String htmlPageAnlageschwerpunkt) {
        String[] splitResponse = htmlPageAnlageschwerpunkt.split("<script id=\"__NEXT_DATA__\" type=\"application/json\">");
        return splitResponse[1].split("</script>")[0];
    }

}
