package de.hsrm.arocclusion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Environment {

    private List<ProxyModel> proxies = new ArrayList<>();
    private List<ReferencePoint> referencePoints = new ArrayList<>();
    private transient ARSubScene arSubScene;

    public List<ProxyModel> getProxies() {
        return proxies;
    }

    public void setProxies(List<ProxyModel> proxies) {
        this.proxies = proxies;
    }

    public List<ReferencePoint> getReferencePoints() {
        return Collections.unmodifiableList(referencePoints);
    }

    public void addReferencePoint(ReferencePoint referencePoint) {
        referencePoints.add(referencePoint);
        referencePoint.setEnvironment(this);
    }

    public void removeReferencePoint(ReferencePoint referencePoint) {
        referencePoints.remove(referencePoint);
        referencePoint.setEnvironment(null);
    }

    public void setReferencePoints(List<ReferencePoint> referencePoints) {
        this.referencePoints = referencePoints;
        for (ReferencePoint referencePoint : referencePoints) {
            referencePoint.setEnvironment(this);
        }
    }

    public <T extends ReferencePoint> List<T> getReferencePointsWithType(Class<T> clazz) {
        List<T> filteredList = new ArrayList<>();
        for (ReferencePoint referencePoint : referencePoints) {
            if (clazz.isInstance(referencePoint)) {
                filteredList.add(clazz.cast(referencePoint));
            }
        }
        return filteredList;
    }

    public boolean hasKnownReferencePointPosition() {
        return getReferencePoints().stream().anyMatch(ReferencePoint::isPositionKnown);
    }

    public ARSubScene getArSubScene() {
        return arSubScene;
    }

    public void setArSubScene(ARSubScene arSubScene) {
        this.arSubScene = arSubScene;
    }

    public void runPostDeserializationProcessing() {
        setReferencePoints(referencePoints);
    }
}
