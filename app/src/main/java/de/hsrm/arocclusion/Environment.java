package de.hsrm.arocclusion;

import java.util.ArrayList;
import java.util.List;

public class Environment {

    private List<ProxyModel> proxies = new ArrayList<>();
    private List<ReferencePoint> referencePoints = new ArrayList<>();

    public List<ProxyModel> getProxies() {
        return proxies;
    }

    public void setProxies(List<ProxyModel> proxies) {
        this.proxies = proxies;
    }

    public List<ReferencePoint> getReferencePoints() {
        return referencePoints;
    }

    public void setReferencePoints(List<ReferencePoint> referencePoints) {
        this.referencePoints = referencePoints;
    }
}
