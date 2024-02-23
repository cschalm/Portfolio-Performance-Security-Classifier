package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.StringUtils.containsIgnoreCase;

public class Security {
    private final String strISIN;
    private String strName;
    private Map<String, PercentageUsedTuple> oListForBranches = new HashMap<>();
    private Map<String, PercentageUsedTuple> oListForHoldings = new HashMap<>();
    private Map<String, PercentageUsedTuple> oListForCurrencies = new HashMap<>();
    private Map<String, PercentageUsedTuple> oListForInstruments = new HashMap<>();
    private Map<String, PercentageUsedTuple> oListForCountries = new HashMap<>();

    public Security(String isin) {
        this.strISIN = isin;
    }

    public void setRest(Map<String, PercentageUsedTuple> oRest) {
        Double dOthers = 100.0;
        for (Map.Entry<String, PercentageUsedTuple> oEntry : oRest.entrySet()) {
            dOthers -= oEntry.getValue().dPerc;
        }
        oRest.put("Andere", new PercentageUsedTuple(dOthers, false));
    }

    /*
     * getter
     */
    public String getName() {
        return this.strName;
    }

    public void setName(String strName) {
        this.strName = strName;
    }

    public String getIsin() {
        return this.strISIN;
    }

    /**
     * Last element is called "Andere" and collects the missing percentage (to
     * 100) not included in the others element
     *
     * @return Map<String, Double>
     */
    public Map<String, PercentageUsedTuple> getBranches() {
        return this.oListForBranches;
    }

    /*
     * setter
     */
    public void setBranches(Map<String, PercentageUsedTuple> oBranches) {
        List<String> toRemove = new ArrayList<>();
        Map<String, PercentageUsedTuple> toAdd = new HashMap<>();
        for (Map.Entry<String, PercentageUsedTuple> entry : oBranches.entrySet()) {
            String strKey = entry.getKey();
            if (containsIgnoreCase(strKey, "service")) {
                toRemove.add(strKey);
                strKey = strKey.toLowerCase().replaceAll("services", "dienste");
                strKey = strKey.toLowerCase().replaceAll("service", "dienste");
                toAdd.put(strKey, entry.getValue());
            }
        }
        for (String strRemove : toRemove) {
            oBranches.remove(strRemove);
        }
        oBranches.putAll(toAdd);
        this.oListForBranches = oBranches;
        //setRest(this.oListForBranches);
    }

    /**
     * Last element is called "Andere" and collects the missing percentage (to
     * 100) not included in the others element
     *
     * @return Map<String, Double>
     */
    public Map<String, PercentageUsedTuple> getHoldings() {
        return this.oListForHoldings;
    }

    public void setHoldings(Map<String, PercentageUsedTuple> oHoldings) {
        this.oListForHoldings = oHoldings;
        //setRest(this.oListForHoldings);
    }

    /**
     * Last element is called "Andere" and collects the missing percentage (to
     * 100) not included in the others element
     *
     * @return Map<String, Double>
     */
    public Map<String, PercentageUsedTuple> getCurrencies() {
        return this.oListForCurrencies;
    }

    public void setCurrencies(Map<String, PercentageUsedTuple> oCurrencies) {
        this.oListForCurrencies = oCurrencies;
        //setRest(this.oListForCurrencies);
    }

    /**
     * Last element is called "Andere" and collects the missing percentage (to
     * 100) not included in the others element
     *
     * @return Map<String, Double>
     */
    public Map<String, PercentageUsedTuple> getInstruments() {
        return this.oListForInstruments;
    }

    public void setInstruments(Map<String, PercentageUsedTuple> oInstruments) {
        this.oListForInstruments = oInstruments;
        //setRest(this.oListForInstruments);
    }

    /**
     * Last element is called "Andere" and collects the missing percentage (to
     * 100) not included in the others element
     *
     * @return Map<String, Double>
     */
    public Map<String, PercentageUsedTuple> getCountries() {
        return this.oListForCountries;
    }

    public void setCountries(Map<String, PercentageUsedTuple> oCountries) {
        this.oListForCountries = oCountries;
        setRest(this.oListForCountries);
    }

    /**
     * returns percentage of given country; if ETF hasn't any percentage in that country => 0.0
     * sets the country as used
     *
     * @return Double
     */
    public Double getPercentageOfCountry(String strCountry) {
        if (this.oListForCountries.containsKey(strCountry)) {
            this.oListForCountries.get(strCountry).fIsUsed = true;
            return this.oListForCountries.get(strCountry).dPerc;
        } else {
            return 0.0;
        }
    }

    /**
     * returns percentage of given Branch; if ETF hasn't any percentage in that Branch => 0.0
     * sets the Branch as used
     *
     * @return Double
     */
    public Double getPercentageOfBranch(String strBranch) {
        if (this.oListForBranches.containsKey(strBranch)) {
            this.oListForBranches.get(strBranch).fIsUsed = true;
            return this.oListForBranches.get(strBranch).dPerc;
        } else {
            return 0.0;
        }
    }

    /**
     * returns percentage of given Instrument; if ETF hasn't any percentage in that Instrument => 0.0
     * sets the Instrument as used
     *
     * @return Double
     */
    public Double getPercentageOfInstrument(String strInstrument) {
        if (this.oListForInstruments.containsKey(strInstrument)) {
            this.oListForInstruments.get(strInstrument).fIsUsed = true;
            return this.oListForInstruments.get(strInstrument).dPerc;
        } else {
            return 0.0;
        }
    }

    /**
     * returns percentage of given holding; if ETF hasn't any percentage in that holding => 0.0
     * sets the Instrument as used
     *
     * @return Double
     */
    public Double getPercentageOfHolding(String strHolding) {
        if (this.oListForHoldings.containsKey(strHolding)) {
            this.oListForHoldings.get(strHolding).fIsUsed = true;
            return this.oListForHoldings.get(strHolding).dPerc;
        } else {
            return 0.0;
        }
    }

    /**
     * returns all unused countries as string; separated by ';'
     * all countries just => empty string
     *
     * @return string
     */
    public String getUnusedCountries() {
        return getUnusedEntriesListed(this.oListForCountries);
    }

    /**
     * returns all unused branches as string; separated by ';'
     * all branches just => empty string
     *
     * @return string
     */
    public String getUnusedBranches() {
        return getUnusedEntriesListed(this.oListForBranches);
    }

    private String getUnusedEntriesListed(Map<String, PercentageUsedTuple> map) {
        StringBuilder strResult = new StringBuilder();
        for (Map.Entry<String, PercentageUsedTuple> oEntry : map.entrySet()) {
            if (!oEntry.getValue().fIsUsed) {
                if (strResult.length() > 0) {
                    strResult.append(';');
                }
                strResult.append(oEntry.getKey());
            }
        }
        return strResult.toString();
    }

    public String[] getAllBranches() {
        return this.oListForBranches.keySet().toArray(new String[0]);
    }

    @Override
    public String toString() {
        if (strName != null)
            return strISIN + ": " + strName;
        return strISIN;
    }

    public static class PercentageUsedTuple {
        Double dPerc;
        Boolean fIsUsed;

        public PercentageUsedTuple(Double dOthers, boolean b) {
            this.dPerc = dOthers;
            this.fIsUsed = b;
        }

        @Override
        public String toString() {
            return String.format("%.2f%%", dPerc);
        }
    }
}
