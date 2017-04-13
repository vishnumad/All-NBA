package com.gmail.jorgegilcavazos.ballislife.features.model;

public class SubscriberCount {

    private Long subscribers;
    private int activeUsers;

    public SubscriberCount(Long subscribers, int activeUsers) {
        this.subscribers = subscribers;
        this.activeUsers = activeUsers;
    }

    public Long getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Long subscribers) {
        this.subscribers = subscribers;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }
}
