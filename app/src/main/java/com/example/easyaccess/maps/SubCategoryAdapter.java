package com.example.easyaccess.maps;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.R;

import java.util.List;

public class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.SubCategoryViewHolder> {
    private List<SubCategory> subCategories;

    public SubCategoryAdapter(List<SubCategory> subCategories) {
        this.subCategories = subCategories;
    }

    @NonNull
    @Override
    public SubCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_category_item, parent, false);
        return new SubCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubCategoryViewHolder holder, int position) {
        SubCategory subCategory = subCategories.get(position);
        holder.subCategoryTitle.setText(subCategory.getTitle());
        holder.subCategoryIcon.setImageResource(android.R.drawable.checkbox_off_background);
        if(subCategory.isChecked()) {
            holder.subCategoryIcon.setImageResource(android.R.drawable.checkbox_on_background);
        }
    }

    @Override
    public int getItemCount() {
        return subCategories.size();
    }

    public static class SubCategoryViewHolder extends RecyclerView.ViewHolder {
        TextView subCategoryTitle;
        ImageView subCategoryIcon;

        public SubCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            subCategoryTitle = itemView.findViewById(R.id.subCategoryTitle);
            subCategoryIcon = itemView.findViewById(R.id.subCategoryIcon);
        }
    }
}
