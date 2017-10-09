package com.gmail.jorgegilcavazos.ballislife.features.model;

import com.google.gson.annotations.SerializedName;

public class Highlight {

    private String id;
    private String title;
    private String thumbnail;
    private String hdThumbnail;
    private String url;
    @SerializedName("created_utc") private long createdUtc;

    public Highlight(String id, String title, String thumbnail, String hdThumbnail, String url,
                     long createdUtc) {
        this.id = id;
        this.title = title;
        this.thumbnail = thumbnail;
        this.hdThumbnail = hdThumbnail;
        this.url = url;
        this.createdUtc = createdUtc;
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

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
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

    public long getCreatedUtc() {
        return createdUtc;
    }

    public void setCreatedUtc(long createdUtc) {
        this.createdUtc = createdUtc;
    }
}
