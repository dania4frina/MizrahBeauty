package com.example.mizrahbeauty.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mizrahbeauty.R;
import com.example.mizrahbeauty.models.Service;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminServiceAdapter extends RecyclerView.Adapter<AdminServiceAdapter.AdminServiceViewHolder> {

    private List<Service> serviceList;
    private Context context;
    private OnServiceActionListener listener;

    public interface OnServiceActionListener {
        void onEditService(Service service, int position);
        void onDeleteService(Service service, int position);
        void onServiceClick(Service service, int position);
    }

    public AdminServiceAdapter(Context context, List<Service> serviceList, OnServiceActionListener listener) {
        this.context = context;
        this.serviceList = serviceList;
        this.listener = listener;
    }

    public void setOnServiceActionListener(OnServiceActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_service_modern, parent, false);
        return new AdminServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);

        // Set service initials
        holder.serviceInitialsText.setText(getInitials(service.getServiceName()));

        // Set service name
        holder.serviceNameText.setText(service.getServiceName());

        // Set category
        holder.categoryText.setText(service.getCategory());
        setCategoryStyle(holder.categoryText, service.getCategory());

        // Set duration
        holder.durationText.setText(formatDuration(service.getDurationMinutes()));

        // Set details
        if (service.getDetails() != null && !service.getDetails().trim().isEmpty()) {
            holder.detailsText.setVisibility(View.VISIBLE);
            holder.detailsText.setText(service.getDetails());
        } else {
            holder.detailsText.setVisibility(View.GONE);
        }

        // Set price
        holder.priceText.setText(formatPrice(service.getPrice()));

        // Set action button listeners
        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditService(service, position);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteService(service, position);
            }
        });
        
        // Set item click listener to show full description
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onServiceClick(service, position);
            }
        });
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "";
        StringBuilder initials = new StringBuilder();
        for (String s : name.split(" ")) {
            if (!s.isEmpty()) {
                initials.append(s.charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }

    private String formatDuration(int minutes) {
        if (minutes <= 0) return "N/A";
        if (minutes < 60) return minutes + " mins";
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        if (remainingMinutes == 0) return hours + "h";
        return hours + "h " + remainingMinutes + "m";
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("ms", "MY")); // Malaysian Ringgit
        return formatter.format(price);
    }

    private void setCategoryStyle(TextView categoryText, String category) {
        if (category == null) category = "UNKNOWN";
        int backgroundColor;
        switch (category.toUpperCase()) {
            case "HAIR":
                backgroundColor = ContextCompat.getColor(context, R.color.primary_pink);
                break;
            case "FACE":
                backgroundColor = ContextCompat.getColor(context, android.R.color.holo_blue_light);
                break;
            case "NAILS":
                backgroundColor = ContextCompat.getColor(context, android.R.color.holo_green_light);
                break;
            case "BRIDAL":
                backgroundColor = ContextCompat.getColor(context, android.R.color.holo_red_light);
                break;
            default:
                backgroundColor = ContextCompat.getColor(context, android.R.color.darker_gray);
                break;
        }
        categoryText.getBackground().setTint(backgroundColor);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class AdminServiceViewHolder extends RecyclerView.ViewHolder {
        TextView serviceInitialsText, serviceNameText, categoryText, durationText, detailsText, priceText;
        ImageView editButton, deleteButton;

        public AdminServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceInitialsText = itemView.findViewById(R.id.serviceInitialsText);
            serviceNameText = itemView.findViewById(R.id.serviceNameText);
            categoryText = itemView.findViewById(R.id.categoryText);
            durationText = itemView.findViewById(R.id.durationText);
            detailsText = itemView.findViewById(R.id.detailsText);
            priceText = itemView.findViewById(R.id.priceText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
