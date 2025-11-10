package com.example.mizrahbeauty.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mizrahbeauty.R;
import com.example.mizrahbeauty.models.Category;

import java.util.List;

public class AdminCategoryListAdapter extends RecyclerView.Adapter<AdminCategoryListAdapter.AdminCategoryViewHolder> {
    private List<Category> categoryList;
    private Context context;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public AdminCategoryListAdapter(List<Category> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_category_simple, parent, false);
        return new AdminCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminCategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class AdminCategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView categoryNameText, serviceCountText;
        private ImageView categoryIcon, editIcon, deleteIcon;

        public AdminCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameText = itemView.findViewById(R.id.categoryNameText);
            serviceCountText = itemView.findViewById(R.id.serviceCountText);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            // Remove edit and delete icons for simple layout
            editIcon = null;
            deleteIcon = null;
        }

        public void bind(Category category) {
            System.out.println("=== ADMIN CATEGORY ADAPTER DEBUG ===");
            System.out.println("Category Name: " + category.getCategoryName());
            System.out.println("Service Count: " + category.getServiceCount());
            
            categoryNameText.setText(category.getCategoryName());
            serviceCountText.setText(category.getServiceCount() + " services");
            
            System.out.println("Text set - Category: " + categoryNameText.getText());
            System.out.println("Text set - Count: " + serviceCountText.getText());

            // Set category icon based on category name
            setCategoryIcon(category.getCategoryName());

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });

            // Edit and delete icons not available in simple layout
        }

        private void setCategoryIcon(String categoryName) {
            int iconRes;
            switch (categoryName.toLowerCase()) {
                case "basic spa":
                    iconRes = R.drawable.ic_spa;
                    break;
                case "face relaxing":
                    iconRes = R.drawable.ic_face;
                    break;
                case "hair salon":
                    iconRes = R.drawable.ic_hair;
                    break;
                case "bridal package":
                    iconRes = R.drawable.ic_bridal;
                    break;
                default:
                    iconRes = R.drawable.ic_service;
                    break;
            }
            categoryIcon.setImageResource(iconRes);
        }
    }
}
