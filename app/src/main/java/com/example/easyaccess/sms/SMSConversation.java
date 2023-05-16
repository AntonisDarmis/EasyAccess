package com.example.easyaccess.sms;

public class SMSConversation {
    private String name,message,photo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public SMSConversation(String name, String message, String photo) {
        this.name = name;
        this.message = message;
        this.photo = photo;
    }
}
