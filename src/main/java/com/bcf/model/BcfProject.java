package com.bcf.model;

import java.util.ArrayList;
import java.util.List;

public class BcfProject {
    private String projectId;
    private String name;
    private String version = "2.1";
    private List<BcfTopic> topics = new ArrayList<>();

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<BcfTopic> getTopics() {
        return topics;
    }

    public void setTopics(List<BcfTopic> topics) {
        this.topics = topics;
    }
}
