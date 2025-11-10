package com.example.mizrahbeauty.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    
    private List<Booking> bookingList;
    private Context context;
    private OnBookingActionListener listener;

    public interface OnBookingActionListener {
        void onCancelBooking(Booking booking, int position);
    }

    public BookingAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    public BookingAdapter(Context context, List<Booking> bookingList, OnBookingActionListener listener) {
        this.context = context;
        this.bookingList = bookingList;
        this.listener = listener;
    }

    public void setOnBookingActionListener(OnBookingActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        
        // Format: "#12 - rebonding | 2025-10-01 13:00:00.0"
        String headerText = "#" + booking.getBookingId() + " - " + booking.getServiceName() + " | " + booking.getBookingDate() + " " + booking.getBookingTime();
        holder.bookingHeaderText.setText(headerText);
        
        // Status display
        holder.statusText.setText(booking.getStatus());
        
        // Show cancel button only for ACTIVE bookings
        if ("ACTIVE".equalsIgnoreCase(booking.getStatus())) {
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelBooking(booking, position);
                }
            });
        } else {
            holder.cancelButton.setVisibility(View.GONE);
        }
        
        // Customer and beautician info
        String customerLabel = booking.getCustomerName();
        if (customerLabel == null || customerLabel.trim().isEmpty()) {
            customerLabel = booking.getCustomerEmail();
        }
        String staffLabel = booking.getStaffName();

        StringBuilder infoBuilder = new StringBuilder();
        if (customerLabel != null && !customerLabel.trim().isEmpty()) {
            infoBuilder.append("Customer: ").append(customerLabel.trim());
        }
        if (staffLabel != null && !staffLabel.trim().isEmpty()) {
            if (infoBuilder.length() > 0) {
                infoBuilder.append("  •  ");
            }
            infoBuilder.append("Beautician: ").append(staffLabel.trim());
        }

        if (infoBuilder.length() > 0) {
            holder.customerEmailText.setVisibility(View.VISIBLE);
            holder.customerEmailText.setText(infoBuilder.toString());
        } else {
            holder.customerEmailText.setVisibility(View.GONE);
        }
        
        // Show notes if available
        if (booking.getNotes() != null && !booking.getNotes().trim().isEmpty()) {
            holder.notesText.setVisibility(View.VISIBLE);
            holder.notesText.setText("Notes: " + booking.getNotes());
        } else {
            holder.notesText.setVisibility(View.GONE);
        }

        // Set status color
        setStatusColor(holder.statusText, booking.getStatus());
        
        // Backwards compatibility - populate hidden fields
        holder.serviceNameText.setText("Service: " + booking.getServiceName());
        holder.appointmentTimeText.setText("Time: " + booking.getBookingDate() + " " + booking.getBookingTime());
    }

    private void setStatusColor(TextView statusText, String status) {
        int color;
        int backgroundColor;
        switch (status.toUpperCase()) {
            case "ACTIVE":
                color = ContextCompat.getColor(context, android.R.color.white);
                backgroundColor = ContextCompat.getColor(context, android.R.color.holo_green_dark);
                break;
            case "CANCELLED":
                color = ContextCompat.getColor(context, android.R.color.white);
                backgroundColor = ContextCompat.getColor(context, android.R.color.holo_red_dark);
                break;
            case "COMPLETED":
                color = ContextCompat.getColor(context, android.R.color.white);
                backgroundColor = ContextCompat.getColor(context, R.color.primary_purple);
                break;
            default:
                color = ContextCompat.getColor(context, android.R.color.white);
                backgroundColor = ContextCompat.getColor(context, android.R.color.darker_gray);
                break;
        }
        statusText.setTextColor(color);
        statusText.setBackgroundColor(backgroundColor);
        statusText.setPadding(16, 8, 16, 8);
    }

    private String formatDateTime(String dateTimeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateTimeString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateTimeString; // Return original string if parsing fails
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public void updateBookings(List<Booking> newBookings) {
        this.bookingList = newBookings;
        notifyDataSetChanged();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView bookingHeaderText, customerEmailText, serviceNameText, appointmentTimeText, statusText, notesText;
        Button cancelButton;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            bookingHeaderText = itemView.findViewById(R.id.bookingHeaderText);
            customerEmailText = itemView.findViewById(R.id.customerEmailText);
            serviceNameText = itemView.findViewById(R.id.serviceNameText);
            appointmentTimeText = itemView.findViewById(R.id.appointmentTimeText);
            statusText = itemView.findViewById(R.id.statusText);
            notesText = itemView.findViewById(R.id.notesText);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }
}
