package com.gmail.jorgegilcavazos.ballislife.features.model.wrapper;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.io.Serializable;

/**
 * Wraps a {@link Submission} to allow mutation.
 */
public class CustomSubmission implements Serializable {

    private String id;
    private Submission submission;
    private String title;
    private String author;
    private long created;
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

    public CustomSubmission(Submission submission) {
        this.submission = submission;
        id = submission.getId();
        title = submission.getTitle();
        author = submission.getAuthor();
        created = submission.getCreated().getTime();
        domain = submission.getDomain();
        selfPost = submission.isSelfPost();
        stickied = submission.isStickied();
        score = submission.getScore();
        commentCount = submission.getCommentCount();
        thumbnail = submission.getThumbnail();
        voteDirection = submission.getVote();
        saved = submission.isSaved();
        selfTextHtml = submission.data("selftext_html");
        url = submission.getUrl();

        try {
            highResThumbnail = submission.getOEmbedMedia().getThumbnail().getUrl().toString();
        } catch (NullPointerException e) {
            highResThumbnail = null;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
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
