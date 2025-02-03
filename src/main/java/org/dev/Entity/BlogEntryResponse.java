package org.dev.Entity;

import java.util.List;

public class BlogEntryResponse {
    private String status;
    private List<BlogEntry> result;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<BlogEntry> getResult() {
        return result;
    }

    public void setResult(List<BlogEntry> result) {
        this.result = result;
    }
}