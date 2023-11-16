package services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.schalm.test.AbstractTest;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class SecurityDetailsTest extends AbstractTest {

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
}