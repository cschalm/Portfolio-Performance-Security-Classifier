package models;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * One share from the portfolio in Portfolio Performance or ETF with name, ISIN and branches, holdings and counties.
 */
public class Security {
    private final String isin;
    private String name;
    private boolean fond;
    private final int indexInPortfolio;
    private Map<String, Double> industriesMap = new HashMap<>();
    private Map<String, Double> holdingsMap = new HashMap<>();
    private Map<String, Double> countriesMap = new HashMap<>();

    public Security(String isin, int indexInPortfolio) {
        this.isin = isin;
        this.indexInPortfolio = indexInPortfolio;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String strName) {
        this.name = strName;
    }

    public String getIsin() {
        return this.isin;
    }

    public Map<String, Double> getIndustries() {
        return this.industriesMap;
    }

    public void setIndustries(Map<String, Double> industriesMap) {
        List<String> toRemove = new ArrayList<>();
        Map<String, Double> toAdd = new HashMap<>();
        for (Map.Entry<String, Double> entry : industriesMap.entrySet()) {
            String strKey = entry.getKey();
            if (StringUtils.containsIgnoreCase(strKey, "service")) {
                toRemove.add(strKey);
                strKey = strKey.toLowerCase().replaceAll("services", "dienste");
                strKey = strKey.toLowerCase().replaceAll("service", "dienste");
                toAdd.put(strKey, entry.getValue());
            }
        }
        for (String strRemove : toRemove) {
            industriesMap.remove(strRemove);
        }
        industriesMap.putAll(toAdd);
        this.industriesMap = industriesMap;
    }

    public Map<String, Double> getHoldings() {
        return this.holdingsMap;
    }

    public void setHoldings(Map<String, Double> holdingsMap) {
        this.holdingsMap = holdingsMap;
    }

    public Map<String, Double> getCountries() {
        return this.countriesMap;
    }

    public void setCountries(Map<String, Double> countriesMap) {
        this.countriesMap = countriesMap;
    }

    /**
     * returns percentage of given country; if ETF hasn't any percentage in that country => 0.0
     * sets the country as used
     *
     * @return Double
     */
    public Double getPercentageOfCountry(String country) {
        return this.countriesMap.getOrDefault(country, 0.0);
    }

    /**
     * returns percentage of given Branch; if ETF hasn't any percentage in that Branch => 0.0
     * sets the Branch as used
     *
     * @return Double
     */
    public Double getPercentageOfBranch(String branch) {
        return this.industriesMap.getOrDefault(branch, 0.0);
    }

    /**
     * returns percentage of given holding; if ETF hasn't any percentage in that holding => 0.0
     * sets the Instrument as used
     *
     * @return Double
     */
    public Double getPercentageOfHolding(String holding) {
        return this.holdingsMap.getOrDefault(holding, 0.0);
    }

    @Override
    public String toString() {
        if (name != null)
            return isin + ": " + name;
        return isin;
    }

    public boolean isFond() {
        return fond;
    }

    public void setFond(boolean fond) {
        this.fond = fond;
    }

    public int getIndexInPortfolio() {
        return indexInPortfolio;
    }

    public static class SecurityComparator implements Comparator<Security> {

        @Override
        public int compare(Security one, Security two) {
            return one.name.compareToIgnoreCase(two.name);
        }

    }

}
