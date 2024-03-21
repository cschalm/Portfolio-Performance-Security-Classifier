package models;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One share from the portfolio in Portfolio Performance or ETF with name, ISIN and branches, holdings and counties.
 */
public class Security {
    private final String isin;
    private String name;
    private boolean fond;
    private Map<String, Double> branchesMap = new HashMap<>();
    private Map<String, Double> holdingsMap = new HashMap<>();
    private Map<String, Double> countriesMap = new HashMap<>();

    public Security(String isin) {
        this.isin = isin;
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

    public Map<String, Double> getBranches() {
        return this.branchesMap;
    }

    public void setBranches(Map<String, Double> branchesMap) {
        List<String> toRemove = new ArrayList<>();
        Map<String, Double> toAdd = new HashMap<>();
        for (Map.Entry<String, Double> entry : branchesMap.entrySet()) {
            String strKey = entry.getKey();
            if (StringUtils.containsIgnoreCase(strKey, "service")) {
                toRemove.add(strKey);
                strKey = strKey.toLowerCase().replaceAll("services", "dienste");
                strKey = strKey.toLowerCase().replaceAll("service", "dienste");
                toAdd.put(strKey, entry.getValue());
            }
        }
        for (String strRemove : toRemove) {
            branchesMap.remove(strRemove);
        }
        branchesMap.putAll(toAdd);
        this.branchesMap = branchesMap;
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
        return this.branchesMap.getOrDefault(branch, 0.0);
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

    public String[] getAllBranches() {
        return this.branchesMap.keySet().toArray(new String[0]);
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
}
