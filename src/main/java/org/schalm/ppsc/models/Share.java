package org.schalm.ppsc.models;

public class Share extends Security {
    public Share(String isin, int indexInPortfolio, boolean active) {
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
        return true;
    }

    @Override
    public boolean isCommodity() {
        return false;
    }
}
