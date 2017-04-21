package com.gmail.jorgegilcavazos.ballislife.features.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Represents content of a Streamable video. More info can be found
 * <a href="https://streamable.com/documentation">here</a>.
 */
public class Streamable implements Serializable {
    private int status;
    private String title;
    private StreamableFiles files;
    private String url;
    private String thumbnail_url;
    private String message;

    public Streamable(int status, String title, StreamableFiles files, String url, String thumbnail_url, String message) {
        this.status = status;
        this.title = title;
        this.files = files;
        this.url = url;
        this.thumbnail_url = thumbnail_url;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public StreamableFiles getFiles() {
        return files;
    }

    public void setFiles(StreamableFiles files) {
        this.files = files;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public void setThumbnail_url(String thumbnail_url) {
        this.thumbnail_url = thumbnail_url;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public class StreamableFiles implements Serializable {
        StreamableFile mp4;

        @SerializedName("mp4-mobile")
        StreamableFile mp4Mobile;

        public StreamableFiles(StreamableFile mp4, StreamableFile mp4Mobile) {
            this.mp4 = mp4;
            this.mp4Mobile = mp4Mobile;
        }

        public StreamableFile getMp4() {
            return mp4;
        }

        public void setMp4(StreamableFile mp4) {
            this.mp4 = mp4;
        }

        public StreamableFile getMp4Mobile() {
            return mp4Mobile;
        }

        public void setMp4Mobile(StreamableFile mp4Mobile) {
            this.mp4Mobile = mp4Mobile;
        }
    }

    public class StreamableFile implements Serializable {
        String url;
        int width;
        int height;

        public StreamableFile(String url, int width, int height) {
            this.url = url;
            this.width = width;
            this.height = height;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }
}
