package com.gmail.jorgegilcavazos.ballislife.features.model.wrapper;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.io.Serializable;

public class CustomSubmission implements Serializable {

    private Submission submission;
    private String title;
    private String author;
    private String timestamp;
    private String domain;
    private boolean selfPost;
    private boolean stickied;
    private int score;
    private int commentCount;
    private String thumbnail;
    private String highResThumbnail;
    private VoteDirection voteDirection;
    private boolean saved;
    private String selfTextHtml;
    private String url;

    public CustomSubmission() {
    }

    public CustomSubmission(Submission submission, VoteDirection voteDirection, boolean saved) {
        this.submission = submission;
        this.voteDirection = voteDirection;
        this.saved = saved;
    }

    public CustomSubmission(String title, String author, String timestamp, String domain,
                            boolean selfPost, boolean stickied, int score, int commentCount,
                            String thumbnail, String highResThumbnail, VoteDirection voteDirection,
                            boolean saved, String selfTextHtml, String url) {
        this.title = title;
        this.author = author;
        this.timestamp = timestamp;
        this.domain = domain;
        this.selfPost = selfPost;
        this.stickied = stickied;
        this.score = score;
        this.commentCount = commentCount;
        this.thumbnail = thumbnail;
        this.highResThumbnail = highResThumbnail;
        this.voteDirection = voteDirection;
        this.saved = saved;
        this.selfTextHtml = selfTextHtml;
        this.url = url;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isSelfPost() {
        return selfPost;
    }

    public void setSelfPost(boolean selfPost) {
        this.selfPost = selfPost;
    }

    public boolean isStickied() {
        return stickied;
    }

    public void setStickied(boolean stickied) {
        this.stickied = stickied;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getHighResThumbnail() {
        return highResThumbnail;
    }

    public void setHighResThumbnail(String highResThumbnail) {
        this.highResThumbnail = highResThumbnail;
    }

    public VoteDirection getVoteDirection() {
        return voteDirection;
    }

    public void setVoteDirection(VoteDirection voteDirection) {
        this.voteDirection = voteDirection;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public String getSelfTextHtml() {
        return selfTextHtml;
    }

    public void setSelfTextHtml(String selfTextHtml) {
        this.selfTextHtml = selfTextHtml;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
