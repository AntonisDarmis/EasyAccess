package com.example.easyaccess.maps;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.R;

import java.util.List;

// CategoryAdapter.java
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.GeneralCategoryViewHolder> {
    private List<GeneralCategory> generalCategories;

    public CategoryAdapter(List<GeneralCategory> generalCategories) {
        this.generalCategories = generalCategories;
    }

    @NonNull
    @Override
    public GeneralCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.general_category_item, parent, false);
        return new GeneralCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GeneralCategoryViewHolder holder, int position) {
        GeneralCategory generalCategory = generalCategories.get(position);
        holder.generalTitle.setText(generalCategory.getTitle());
        SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(generalCategory.getSubCategories());
        holder.subCategoryRecyclerView.setAdapter(subCategoryAdapter);
    }

    @Override
    public int getItemCount() {
        return generalCategories.size();
    }

    public static class GeneralCategoryViewHolder extends RecyclerView.ViewHolder {
        TextView generalTitle;
        RecyclerView subCategoryRecyclerView;

        public GeneralCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            generalTitle = itemView.findViewById(R.id.generalTitle);
            subCategoryRecyclerView = itemView.findViewById(R.id.subCategoryRecyclerView);
            // Set up GridLayoutManager with 2 columns
            GridLayoutManager layoutManager = new GridLayoutManager(itemView.getContext(), 2);
            subCategoryRecyclerView.setLayoutManager(layoutManager);
        }
    }
}


