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
                {"DE000TUAG505", "Hotels, Restaurants und Freizeit", "Deutschland", "TUI"},
                {"DE0008402215", "Versicherung", "Deutschland", "Hannover Rück"},
                {"DE000A2NB650", "Finanzdienstleistungen", "Deutschland", "Mutares"},
                {"KYG9830T1067", "Hardware- Technologie, Speicherung und Peripheriegeräte", "China", "Xiaomi"},
                {"NL0011821202", "Banken", "Niederlande", "ING Group"},
                {"US04010L1035", "Kapitalmärkte", "USA", "Ares Capital"},
                {"US0357108390", "Hypotheken-Immobilien-fonds (REITs)", "USA", "Annaly Capital Management"},
                {"US0378331005", "Hardware- Technologie, Speicherung und Peripheriegeräte", "USA", "Apple"},
                {"US5949181045", "Software", "USA", "Microsoft"},
                {"US6819361006", "Diversifizierte REITs", "USA", "Omega Healthcare Invest."},
                {"DE000A1J5RX9", "Drahtlose Telekommunikationsdienste", "Deutschland", "Telefónica Deutschland"}
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
//        assertEquals(branch, securityDetails.getBranchForSecurity());
        assertEquals(branch, securityDetails.getIndustry());
        assertEquals(country, securityDetails.getCountryForSecurity());
        assertEquals(name, securityDetails.getName());
    }

}