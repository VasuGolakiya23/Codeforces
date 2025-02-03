package org.dev.Entity;

import java.util.List;

public class BlogEntry {

    private int id;
    private String originalLocale;
    private int creationTimeSeconds;
    private String authorHandle;
    private String title;
    private String content;
    private String locale;
    private int modificationTimeSeconds;
    private boolean allowViewHistory;
    private List<String> tags;
    private int rating;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOriginalLocale() {
        return originalLocale;
    }

    public void setOriginalLocale(String originalLocale) {
        this.originalLocale = originalLocale;
    }

    public int getCreationTimeSeconds() {
        return creationTimeSeconds;
    }

    public void setCreationTimeSeconds(int creationTimeSeconds) {
        this.creationTimeSeconds = creationTimeSeconds;
    }

    public String getAuthorHandle() {
        return authorHandle;
    }

    public void setAuthorHandle(String authorHandle) {
        this.authorHandle = authorHandle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public int getModificationTimeSeconds() {
        return modificationTimeSeconds;
    }

    public void setModificationTimeSeconds(int modificationTimeSeconds) {
        this.modificationTimeSeconds = modificationTimeSeconds;
    }

    public boolean isAllowViewHistory() {
        return allowViewHistory;
    }

    public void setAllowViewHistory(boolean allowViewHistory) {
        this.allowViewHistory = allowViewHistory;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}