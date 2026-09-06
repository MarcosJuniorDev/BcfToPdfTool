package com.bcf.model;

import java.util.ArrayList;
import java.util.List;

public class BcfTopic {
    private String guid;
    private String topicType;
    private String topicStatus;
    private String title;
    private String priority;
    private Integer index;
    private List<String> labels = new ArrayList<>();
    private String creationDate;
    private String creationAuthor;
    private String modifiedDate;
    private String modifiedAuthor;
    private String description;
    private String assignedTo;
    private String dueDate;
    private String stage;

    private List<String> headerFiles = new ArrayList<>();
    private List<BcfComment> comments = new ArrayList<>();
    private List<BcfViewpoint> viewpoints = new ArrayList<>();

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTopicType() {
        return topicType;
    }

    public void setTopicType(String topicType) {
        this.topicType = topicType;
    }

    public String getTopicStatus() {
        return topicStatus;
    }

    public void setTopicStatus(String topicStatus) {
        this.topicStatus = topicStatus;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreationAuthor() {
        return creationAuthor;
    }

    public void setCreationAuthor(String creationAuthor) {
        this.creationAuthor = creationAuthor;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedAuthor() {
        return modifiedAuthor;
    }

    public void setModifiedAuthor(String modifiedAuthor) {
        this.modifiedAuthor = modifiedAuthor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public List<String> getHeaderFiles() {
        return headerFiles;
    }

    public void setHeaderFiles(List<String> headerFiles) {
        this.headerFiles = headerFiles;
    }

    public List<BcfComment> getComments() {
        return comments;
    }

    public void setComments(List<BcfComment> comments) {
        this.comments = comments;
    }

    public List<BcfViewpoint> getViewpoints() {
        return viewpoints;
    }

    public void setViewpoints(List<BcfViewpoint> viewpoints) {
        this.viewpoints = viewpoints;
    }
}
