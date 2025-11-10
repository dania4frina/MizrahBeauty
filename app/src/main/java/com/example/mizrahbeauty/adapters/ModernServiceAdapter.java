package com.example.mizrahbeauty.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mizrahbeauty.R;
import com.example.mizrahbeauty.models.Service;
import java.util.List;

public class ModernServiceAdapter extends RecyclerView.Adapter<ModernServiceAdapter.ModernServiceViewHolder> {
    
    private List<Service> serviceList;
    private Context context;
    private OnServiceActionListener listener;

    public interface OnServiceActionListener {
        void onBookService(Service service, int position);
        void onServiceClick(Service service, int position);
    }

    public ModernServiceAdapter(Context context, List<Service> serviceList) {
        this.context = context;
        this.serviceList = serviceList;
    }

    public ModernServiceAdapter(Context context, List<Service> serviceList, OnServiceActionListener listener) {
        this.context = context;
        this.serviceList = serviceList;
        this.listener = listener;
    }

    public void setOnServiceActionListener(OnServiceActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ModernServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_service_modern, parent, false);
        return new ModernServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModernServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);
        
        // Set service icon (first 2 letters of service name)
        String serviceName = service.getServiceName();
        String iconText = getServiceIcon(serviceName);
        holder.serviceIconText.setText(iconText);
        
        // Set service name
        holder.serviceNameText.setText(serviceName);
        
        // Set category
        String category = service.getCategory();
        if (category != null && !category.isEmpty()) {
            holder.categoryText.setVisibility(View.VISIBLE);
            holder.categoryText.setText(category);
        } else {
            holder.categoryText.setVisibility(View.GONE);
        }
        
        // Set duration
        holder.durationText.setText(service.getFormattedDuration());
        
        // Set details (optional)
        if (service.getDetails() != null && !service.getDetails().isEmpty()) {
            holder.detailsText.setVisibility(View.VISIBLE);
            holder.detailsText.setText(service.getDetails());
        } else {
            holder.detailsText.setVisibility(View.GONE);
        }
        
        // Set price
        holder.priceText.setText(service.getFormattedPrice());
        
        // Set book button click listener
        holder.bookButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookService(service, position);
            }
        });
        
        // Set item click listener to show description
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onServiceClick(service, position);
            }
        });
    }

    private String getServiceIcon(String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            return "SV";
        }
        
        String[] words = serviceName.trim().split("\\s+");
        if (words.length >= 2) {
            return (words[0].substring(0, 1) + words[1].substring(0, 1)).toUpperCase();
        } else {
            return serviceName.length() >= 2 ? 
                serviceName.substring(0, 2).toUpperCase() : 
                serviceName.toUpperCase();
        }
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public void updateServices(List<Service> newServices) {
        this.serviceList = newServices;
        notifyDataSetChanged();
    }

    public static class ModernServiceViewHolder extends RecyclerView.ViewHolder {
        TextView serviceIconText, serviceNameText, categoryText, durationText, detailsText, priceText;
        Button bookButton;

        public ModernServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceIconText = itemView.findViewById(R.id.serviceIconText);
            serviceNameText = itemView.findViewById(R.id.serviceNameText);
            categoryText = itemView.findViewById(R.id.categoryText);
            durationText = itemView.findViewById(R.id.durationText);
            detailsText = itemView.findViewById(R.id.detailsText);
            priceText = itemView.findViewById(R.id.priceText);
            bookButton = itemView.findViewById(R.id.bookButton);
        }
    }
}
