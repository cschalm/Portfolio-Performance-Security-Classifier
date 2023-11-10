package services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.schalm.test.AbstractTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class SecurityDetailsTest extends AbstractTest {
    private static final Logger logger = Logger.getLogger(SecurityDetailsTest.class.getCanonicalName());

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"DE000TUAG505", "Sonstige Branchen", "Deutschland"},
                {"DE0008402215", "Versicherungen", "Deutschland"},
                {"DE000A2NB650", "Finanzdienstleistungen", "Deutschland"},
                {"KYG9830T1067", "Unterhaltungselektronik", "China"},
                {"NL0011821202", "Banken", "Niederlande"},
                {"US04010L1035", "", "Vereinigte Staaten"},
                {"US0357108390", "Immobilien", "Vereinigte Staaten"},
                {"US0378331005", "Unterhaltungselektronik", "USA"},
                {"US5949181045", "Standardsoftware", "USA"},
                {"US6819361006", "Immobilien", "USA"},
                {"DE000A1J5RX9", "Telekomdienstleister", "Deutschland"}
        });
    }

    private final String isin;
    private final String branch;
    private final String country;

    public SecurityDetailsTest(String isin, String branch, String country) {
        this.isin = isin;
        this.branch = branch;
        this.country = country;
    }

    @Test
    public void createSecurityDetailsFromCache() throws IOException, InterruptedException {
        SecurityDetails securityDetails = new SecurityDetails(BASE_TEST_PATH + "cache/", isin);
        assertNotNull(securityDetails);
        assertNotNull(securityDetails.getRootNode());
        logger.info(isin + ": " + securityDetails.getRootNode()
                .getAsJsonObject("props")
                .getAsJsonObject("pageProps")
                .getAsJsonObject("data")
                .getAsJsonObject("snapshot")
                .getAsJsonObject("company").toString());
        assertEquals(branch, securityDetails.getBranchForSecurity());
        assertEquals(country, securityDetails.getCountryForSecurity());
    }

//    @Test
//    @Ignore
//    public void createTuiFromCache() throws IOException, InterruptedException {
//        String isin = "DE000TUAG505";
//        SecurityDetails tui = new SecurityDetails(BASE_TEST_PATH + "cache/", isin);
//        assertNotNull(tui);
//        assertNotNull(tui.getRootNode());
////        logger.info(tui.getRootNode().toString());
//        assertEquals("Sonstige Branchen", tui.getBranchForSecurity());
//        assertEquals("Deutschland", tui.getCountryForSecurity());
//    }
//
//    @Test
//    @Ignore
//    public void createTuiFromRequest() throws IOException, InterruptedException {
//        String isin = "DE000TUAG505";
//        File tempFile = File.createTempFile("PPEI", ".json");
//        SecurityDetails tui = new SecurityDetails(tempFile.getParent() + "/cache/", isin);
//        assertNotNull(tui);
//        assertNotNull(tui.getRootNode());
////        logger.info(tui.getRootNode().toString());
//        assertEquals("Sonstige Branchen", tui.getBranchForSecurity());
//        assertEquals("Deutschland", tui.getCountryForSecurity());
//    }
//
//    @Test
//    @Ignore
//    public void createHreFromCache() throws IOException, InterruptedException {
//        String isin = "DE0008402215";
//        SecurityDetails tui = new SecurityDetails(BASE_TEST_PATH + "cache/", isin);
//        assertNotNull(tui);
//        assertNotNull(tui.getRootNode());
////        logger.info(tui.getRootNode().toString());
//        assertEquals("Versicherungen", tui.getBranchForSecurity());
//        assertEquals("Deutschland", tui.getCountryForSecurity());
//    }
//
//    @Test
//    @Ignore
//    public void createTelefonicaFromCache() throws IOException, InterruptedException {
//        String isin = "DE000A1J5RX9";
//        SecurityDetails telefonica = new SecurityDetails(BASE_TEST_PATH + "cache/", isin);
//        assertNotNull(telefonica);
//        assertNotNull(telefonica.getRootNode());
////        logger.info(telefonica.getRootNode().toString());
//        assertEquals("Telekomdienstleister", telefonica.getBranchForSecurity());
//        assertEquals("Deutschland", telefonica.getCountryForSecurity());
//    }

}