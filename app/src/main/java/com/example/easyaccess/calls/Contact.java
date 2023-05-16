package com.example.easyaccess.calls;

import java.io.Serializable;
import java.util.ArrayList;

public class Contact implements Serializable {
    private String name;
    private String phone;
    private String photo;

    private String date,dir;



    public Contact(String name, String photo, String date, String dir) {
        this.name = name;
        this.photo = photo;
        this.date = date;
        this.dir = dir;
    }

    public Contact(String name, String phone, String photo) {
        this.name = name;
        this.phone = phone;
        this.photo = photo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
