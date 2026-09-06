package com.bcf.model;

import java.util.ArrayList;
import java.util.List;

public class BcfComment {
    private String guid;
    private String date;
    private String author;
    private String comment;
    private String modifiedDate;
    private String modifiedAuthor;
    private String status;
    private String priority;
    private String viewpointGuid;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getViewpointGuid() {
        return viewpointGuid;
    }

    public void setViewpointGuid(String viewpointGuid) {
        this.viewpointGuid = viewpointGuid;
    }
}
