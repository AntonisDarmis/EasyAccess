package com.example.easyaccess.sms;

public class Message {
   private String message,time,name,profileUrl;
   int type;

    public Message(String message, String time, String name, String profileUrl,int type) {
        this.message = message;
        this.time = time;
        this.name = name;
        this.profileUrl = profileUrl;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}
