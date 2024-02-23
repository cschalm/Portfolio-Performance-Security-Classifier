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
public class SecurityDetailsParamtererizedTest extends AbstractTest {
    private static final Logger logger = Logger.getLogger(SecurityDetailsParamtererizedTest.class.getCanonicalName());
    private final String isin;
    private final String branch;
    private final String country;
    private final String name;

    public SecurityDetailsParamtererizedTest(String isin, String branch, String country, String name) {
        this.isin = isin;
        this.branch = branch;
        this.country = country;
        this.name = name;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"DE000TUAG505", "Sonstige Branchen", "Deutschland", "TUI AG"},
                {"DE0008402215", "Versicherungen", "Deutschland", "HANNOVER RUECK SE"},
                {"DE000A2NB650", "Finanzdienstleistungen", "Deutschland", "MUTARES SE & CO. KGAA"},
                {"KYG9830T1067", "Unterhaltungselektronik", "China", "XIAOMI CORP."},
                {"NL0011821202", "Banken", "Niederlande", "ING GROEP NV"},
                {"US04010L1035", "", "USA", "ARES CAPITAL CORP"},
                {"US0357108390", "Immobilien", "USA", "ANNALY CAPITAL MANAGEMENT"},
                {"US0378331005", "Unterhaltungselektronik", "USA", "APPLE INC."},
                {"US5949181045", "Standardsoftware", "USA", "MICROSOFT CORP."},
                {"US6819361006", "Immobilien", "USA", "OMEGA HEALTHCARE INVESTORS INC."}//,
//                {"DE000A1J5RX9", "Telekomdienstleister", "Deutschland", "TELEFÓNICA DEUTSCHLAND HOLDING AG"}
        });
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
        assertEquals(name, securityDetails.getCompanyNameForSecurity());
    }

}