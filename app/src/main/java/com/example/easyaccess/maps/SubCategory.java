package com.example.easyaccess.maps;

public class SubCategory {
    private String title;
    private int icon;

    private boolean isChecked;

    public SubCategory(String title, int icon) {
        this.title = title;
        this.icon = icon;
        this.isChecked = false;
    }
    public SubCategory(String title) {
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public int getIcon() {
        return icon;
    }

}