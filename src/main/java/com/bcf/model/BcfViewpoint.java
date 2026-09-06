package com.bcf.model;

public class BcfViewpoint {
    private String guid;
    private String viewpointFile;
    private String snapshotFile;
    private byte[] snapshotData;
    private Integer index;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getViewpointFile() {
        return viewpointFile;
    }

    public void setViewpointFile(String viewpointFile) {
        this.viewpointFile = viewpointFile;
    }

    public String getSnapshotFile() {
        return snapshotFile;
    }

    public void setSnapshotFile(String snapshotFile) {
        this.snapshotFile = snapshotFile;
    }

    public byte[] getSnapshotData() {
        return snapshotData;
    }

    public void setSnapshotData(byte[] snapshotData) {
        this.snapshotData = snapshotData;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}
