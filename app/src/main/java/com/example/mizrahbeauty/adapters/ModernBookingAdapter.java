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
import com.example.mizrahbeauty.models.Booking;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ModernBookingAdapter extends RecyclerView.Adapter<ModernBookingAdapter.ModernBookingViewHolder> {
    
    private List<Booking> bookingList;
    private Context context;
    private OnBookingActionListener listener;
    private boolean isUserView = false;

    public interface OnBookingActionListener {
        void onBookingAction(Booking booking, int position);
    }

    public ModernBookingAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    public ModernBookingAdapter(Context context, List<Booking> bookingList, OnBookingActionListener listener) {
        this.context = context;
        this.bookingList = bookingList;
        this.listener = listener;
    }

    public void setOnBookingActionListener(OnBookingActionListener listener) {
        this.listener = listener;
    }

    public void setUserView(boolean isUserView) {
        this.isUserView = isUserView;
    }

    @NonNull
    @Override
    public ModernBookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_modern, parent, false);
        return new ModernBookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModernBookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        
        // Set service icon (first 2 letters of service name)
        String serviceName = booking.getServiceName();
        String iconText = getServiceIcon(serviceName);
        holder.serviceIconText.setText(iconText);
        
        // Set service name
        holder.serviceNameText.setText(serviceName);
        
        // Format and set date/time
        String formattedDateTime = booking.getBookingDate() + " " + booking.getBookingTime();
        holder.dateTimeText.setText(formattedDateTime);
        
        // Set beautician/customer info
        if (isUserView) {
            // User view: Show beautician name
            if (booking.getStaffName() != null && !booking.getStaffName().isEmpty()) {
                holder.customerText.setVisibility(View.VISIBLE);
                holder.customerText.setText("Beautician: " + booking.getStaffName());
            } else {
                holder.customerText.setVisibility(View.GONE);
            }
        } else {
            // Staff view: Show customer name/email
            String customerLabel = booking.getCustomerName();
            if (customerLabel == null || customerLabel.trim().isEmpty()) {
                customerLabel = booking.getCustomerEmail();
            }
            if (customerLabel != null && !customerLabel.trim().isEmpty()) {
                holder.customerText.setVisibility(View.VISIBLE);
                holder.customerText.setText("Customer: " + customerLabel);
            } else {
                holder.customerText.setVisibility(View.GONE);
            }
        }
        
        // Show notes if available
        if (booking.getNotes() != null && !booking.getNotes().trim().isEmpty()) {
            holder.notesText.setVisibility(View.VISIBLE);
            holder.notesText.setText(booking.getNotes());
        } else {
            holder.notesText.setVisibility(View.GONE);
        }
        
        // Set status
        String status = booking.getStatus() != null ? booking.getStatus() : "UNKNOWN";
        holder.statusText.setText(status);
        setStatusStyle(holder.statusText, status);
        
        // Set action button click listener
        holder.actionButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookingAction(booking, position);
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

    private String formatDateTime(String dateTimeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateTimeString);
            
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateTimeString;
        }
    }

    private void setStatusStyle(TextView statusText, String status) {
        if (status == null) status = "UNKNOWN";
        
        int backgroundColor;
        switch (status.toUpperCase()) {
            case "ACTIVE":
                backgroundColor = ContextCompat.getColor(context, android.R.color.holo_green_dark);
                break;
            case "CANCELLED":
                backgroundColor = ContextCompat.getColor(context, android.R.color.holo_red_dark);
                break;
            case "COMPLETED":
                backgroundColor = ContextCompat.getColor(context, R.color.primary_purple);
                break;
            default:
                backgroundColor = ContextCompat.getColor(context, android.R.color.darker_gray);
                break;
        }
        statusText.getBackground().setTint(backgroundColor);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public void updateBookings(List<Booking> newBookings) {
        this.bookingList = newBookings;
        notifyDataSetChanged();
    }

    public static class ModernBookingViewHolder extends RecyclerView.ViewHolder {
        TextView serviceIconText, serviceNameText, dateTimeText, customerText, statusText, notesText;
        ImageView actionButton;

        public ModernBookingViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceIconText = itemView.findViewById(R.id.serviceIconText);
            serviceNameText = itemView.findViewById(R.id.serviceNameText);
            dateTimeText = itemView.findViewById(R.id.dateTimeText);
            customerText = itemView.findViewById(R.id.customerText);
            statusText = itemView.findViewById(R.id.statusText);
            notesText = itemView.findViewById(R.id.notesText);
            actionButton = itemView.findViewById(R.id.actionButton);
        }
    }
}
