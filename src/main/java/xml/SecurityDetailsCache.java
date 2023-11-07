package xml;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.logging.Logger;

public class SecurityDetailsCache {
    private static final Logger logger = Logger.getLogger(SecurityDetailsCache.class.getCanonicalName());
    private final String fullFileName;
    private JsonObject cacheFileJson;

    public SecurityDetailsCache(String fullFileName) {
        this.fullFileName = fullFileName;
        try {
            // if there is an entry in the cache-file, nothing is imported !!!
            cacheFileJson = JsonParser.parseReader(new FileReader(fullFileName)).getAsJsonObject();
        } catch (Exception e) {
            logger.warning("Cache of security details could not be read: " + e.getMessage());
            cacheFileJson = new JsonObject();
            setCachedBranches(new JsonArray());
            setCachedCountries(new JsonArray());
            setCachedTopTen(new JsonArray());
        }
    }

    public JsonObject getCacheFileJson() {
        return cacheFileJson;
    }

    public JsonArray getCachedCountries() {
        return cacheFileJson.get("countries").getAsJsonArray();
    }

    public JsonArray getCachedBranches() {
        return cacheFileJson.get("branches").getAsJsonArray();
    }

    public JsonArray getCachedTopTen() {
        return cacheFileJson.get("topten").getAsJsonArray();
    }

    public void setCachedCountries(JsonArray cachedCountries) {
        cacheFileJson.add("countries", cachedCountries);
    }

    public void setCachedBranches(JsonArray cachedBranches) {
        cacheFileJson.add("branches", cachedBranches);
    }

    public void setCachedTopTen(JsonArray cachedTopTen) {
        cacheFileJson.add("topten", cachedTopTen);
    }

    public void save() {
        // write all saved triples to avoid importing the same assignments several times for each run
        try (PrintWriter savingImport = new PrintWriter(fullFileName)) {
            savingImport.print(getCacheFileJson() + "\n");
        } catch (FileNotFoundException e) {
            logger.warning("Cache of security details could not be saved: " + e.getMessage());
        }
    }

}
