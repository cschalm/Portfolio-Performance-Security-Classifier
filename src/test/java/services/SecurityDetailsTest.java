package services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.SecurityDetailsCache;
import models.SecurityType;
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
        assertEquals("Branches", 9, securityDetailsCache.getCachedIndustries().asList().size());
        assertEquals("Top 10", 10, securityDetailsCache.getCachedTopTen().asList().size());
    }

    @Test
    public void loadFromCache_IE000CNSFAR2() {
        SecurityDetailsCache securityDetailsCache = new SecurityDetailsCache(BASE_TEST_PATH + "XmlFileWriterTest/IE000CNSFAR2.json");
        assertEquals("Countries", 33, securityDetailsCache.getCachedCountries().asList().size());
        assertEquals("Branches", 11, securityDetailsCache.getCachedIndustries().asList().size());
        assertEquals("Top 10", 9, securityDetailsCache.getCachedTopTen().asList().size());
    }

    @Test
    public void readAresPageFromParquet() throws IOException, InterruptedException {
        String isin = "US04010L1035";
        SecurityDetails securityDetails = new SecurityDetails(BASE_TARGET_PATH + "cache/", isin);
        securityDetails.loadSecurityMetaData();
        assertEquals("Kapitalmärkte", securityDetails.getIndustry());
        assertEquals("Ares Capital", securityDetails.getName());
        assertEquals("USA", securityDetails.getCountry());
    }

    @Test
    public void readAresSecurityTypeShareFromParquet() throws IOException, InterruptedException {
        String isin = "US04010L1035";
        SecurityDetails securityDetails = new SecurityDetails(BASE_TARGET_PATH + "cache/", isin);
        SecurityType securityType = securityDetails.loadSecurityType();
        assertEquals(SecurityType.SHARE, securityType);
    }

    @Test
    public void readMsciWorldSecurityTypeEtfFromParquet() throws IOException, InterruptedException {
        String isin = "IE000CNSFAR2";
        SecurityDetails securityDetails = new SecurityDetails(BASE_TARGET_PATH + "cache/", isin);
        SecurityType securityType = securityDetails.loadSecurityType();
        assertEquals(SecurityType.ETF, securityType);
    }

    @Test
    public void readGlobalSmallCapSecurityTypeFondsFromParquet() throws IOException, InterruptedException {
        String isin = "IE00B42W4L06";
        SecurityDetails securityDetails = new SecurityDetails(BASE_TARGET_PATH + "cache/", isin);
        SecurityType securityType = securityDetails.loadSecurityType();
        assertEquals(SecurityType.FONDS, securityType);
    }

    @Test
    public void readGoldPageFromParquet() throws IOException, InterruptedException {
        String isin = "XC0009655157";
        SecurityDetails securityDetails = new SecurityDetails(BASE_TARGET_PATH + "cache/", isin);
        securityDetails.loadSecurityMetaData();
        assertEquals("", securityDetails.getIndustry());
        assertEquals("", securityDetails.getName());
        assertEquals("", securityDetails.getCountry());
    }

}