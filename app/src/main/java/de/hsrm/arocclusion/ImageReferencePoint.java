package de.hsrm.arocclusion;

public class ImageReferencePoint extends ReferencePoint {

    public static final String TYPE = "ImageReferencePoint";
    private String fileName;

    public ImageReferencePoint(String fileName) {
        this();
        this.fileName = fileName;
    }

    public ImageReferencePoint() {
        super(TYPE);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
