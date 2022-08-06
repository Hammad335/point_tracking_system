package com.codewithhamad.muetbustracker.models;

public class Notification {
    String notificationText, notificationSubText;
    boolean isRead;

    public Notification(String notificationText, String notificationSubText, boolean isRead) {
        this.notificationText = notificationText;
        this.notificationSubText = notificationSubText;
        this.isRead = isRead;
    }

    public String getNotificationText() {
        return notificationText;
    }

    public void setNotificationText(String notificationText) {
        this.notificationText = notificationText;
    }

    public String getNotificationSubText() {
        return notificationSubText;
    }

    public void setNotificationSubText(String notificationSubText) {
        this.notificationSubText = notificationSubText;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
