package com.example.easyaccess.sms;

public class SMSConversation {
    private String name, message, photo, date;

    // private int readStatus;

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

//    public int getReadStatus() {
//        return readStatus;
//    }
//
////    public void setReadStatus(int readStatus) {
////        this.readStatus = readStatus;
////    }
//
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public SMSConversation(String name, String message, String photo,String date) {
        this.name = name;
        this.message = message;
        this.photo = photo;
        //  this.readStatus = readStatus;
        this.date = date;

    }
}
