package org.schalm.ppsc.models;

public class Commodity extends Security {
    public Commodity(String isin, int indexInPortfolio, boolean active) {
        super(isin, indexInPortfolio, active);
    }

    @Override
    public boolean isETF() {
        return false;
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
        return true;
    }
}
