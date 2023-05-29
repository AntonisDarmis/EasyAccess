package com.example.easyaccess.maps;

import java.util.List;

// GeneralCategory.java
public class GeneralCategory {
    private String title;
    private List<SubCategory> subCategories;

    public GeneralCategory(String title, List<SubCategory> subCategories) {
        this.title = title;
        this.subCategories = subCategories;
    }

    public String getTitle() {
        return title;
    }

    public List<SubCategory> getSubCategories() {
        return subCategories;
    }
}

// SubCategory.java
