package org.schalm.ppsc.models;

public class ETF extends Security {
    public ETF(String isin, int indexInPortfolio, boolean active) {
        super(isin, indexInPortfolio, active);
    }

    @Override
    public boolean isETF() {
        return true;
    }

    @Override
    public boolean isFund() {
        return false;
    }

    @Override
    public boolean isShare() {
        return false;
    }

    @Override
    public boolean isCommodity() {
        return false;
    }
}
