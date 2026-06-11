package org.schalm.ppsc.models;

public class Fund extends Security {
    public Fund(String isin, int indexInPortfolio, boolean active) {
        super(isin, indexInPortfolio, active);
    }

    @Override
    public boolean isETF() {
        return false;
    }

    @Override
    public boolean isFund() {
        return true;
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
