package com.gmail.jorgegilcavazos.ballislife.features.model;

public class Highlight {

    private String id;
    private String title;
    private String hdThumbnail;
    private String url;

    public Highlight(String id, String title, String hdThumbnail, String url) {
        this.id = id;
        this.title = title;
        this.hdThumbnail = hdThumbnail;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHdThumbnail() {
        return hdThumbnail;
    }

    public void setHdThumbnail(String hdThumbnail) {
        this.hdThumbnail = hdThumbnail;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
