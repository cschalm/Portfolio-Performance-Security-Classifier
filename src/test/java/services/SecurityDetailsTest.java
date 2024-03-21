package services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.SecurityDetailsCache;
import org.junit.Test;
import org.schalm.test.AbstractTest;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static constants.PathConstants.BASE_TARGET_PATH;
import static org.junit.Assert.*;

public class SecurityDetailsTest extends AbstractTest {
    private static final Logger logger = Logger.getLogger(SecurityDetailsTest.class.getCanonicalName());

    @Test
    public void readStringFromURL() throws IOException, InterruptedException {
        SecurityDetails securityDetails = new SecurityDetails(BASE_TEST_PATH + "cache/", "DE000TUAG505");
        assertNotNull(securityDetails);
        assertNotNull(securityDetails.getRootNode());
        String pageContent = securityDetails.readStringFromURL(String.valueOf(new File(BASE_TEST_PATH + "testPage.html").toPath().toUri().toURL()));
        assertNotNull(pageContent);
        assertFalse(pageContent.isEmpty());
        assertTrue(pageContent.length() > 100);
    }

    @Test
    public void getBreakDownForSecurity() throws IOException, InterruptedException {
        SecurityDetails securityDetails = new SecurityDetails(BASE_TEST_PATH + "cache/", "IE00BYYHSM20");
        JsonObject breakDownForSecurity = securityDetails.getBreakDownForSecurity();
        assertNotNull(breakDownForSecurity);
        assertNotNull(breakDownForSecurity.getAsJsonObject("branchBreakdown"));
    }

    @Test
    public void extractJsonPartFromHtml() throws IOException, InterruptedException {
        SecurityDetails securityDetails = new SecurityDetails(BASE_TEST_PATH + "cache/", "DE000TUAG505");
        String pageContent = securityDetails.readStringFromURL(String.valueOf(new File(BASE_TEST_PATH + "testPage.html").toPath().toUri().toURL()));
        String jsonString = securityDetails.extractJsonPartFromHtml(pageContent);
        assertNotNull(jsonString);
        assertFalse(jsonString.isEmpty());
        assertTrue(jsonString.length() > 100);
        JsonElement jsonElement = JsonParser.parseString(jsonString);
        assertNotNull(jsonElement);
    }

    @Test
    public void loadFromCache_IE00BYYHSM20() {
        SecurityDetailsCache securityDetailsCache = new SecurityDetailsCache(BASE_TEST_PATH + "XmlFileWriterTest/IE00BYYHSM20.json");
        assertEquals("Countries", 14, securityDetailsCache.getCachedCountries().asList().size());
        assertEquals("Branches", 9, securityDetailsCache.getCachedBranches().asList().size());
        assertEquals("Top 10", 10, securityDetailsCache.getCachedTopTen().asList().size());
    }

    @Test
    public void loadFromCache_IE000CNSFAR2() {
        SecurityDetailsCache securityDetailsCache = new SecurityDetailsCache(BASE_TEST_PATH + "XmlFileWriterTest/IE000CNSFAR2.json");
        assertEquals("Countries", 33, securityDetailsCache.getCachedCountries().asList().size());
        assertEquals("Branches", 11, securityDetailsCache.getCachedBranches().asList().size());
        assertEquals("Top 10", 9, securityDetailsCache.getCachedTopTen().asList().size());
    }

    @Test
    public void readAresPageFromParquet() throws IOException, InterruptedException {
        String isin = "US04010L1035";
        SecurityDetails securityDetails = new SecurityDetails(BASE_TARGET_PATH + "cache/", isin);
        securityDetails.loadBranchAndDisplayName();
        assertEquals("Kapitalmärkte", securityDetails.getBranch());
        assertEquals("Ares Capital", securityDetails.getName());
    }

}