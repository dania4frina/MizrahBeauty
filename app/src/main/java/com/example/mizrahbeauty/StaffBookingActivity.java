package com.example.mizrahbeauty;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mizrahbeauty.adapters.ModernBookingAdapter;
import com.example.mizrahbeauty.models.Booking;

import java.util.ArrayList;
import java.util.List;

public class StaffBookingActivity extends AppCompatActivity {
    
    private RecyclerView bookingRecyclerView;
    private ModernBookingAdapter adapter;
    private List<Booking> bookingList;
    private List<Booking> allBookingsList; // Store all bookings for filtering
    private TextView titleText, subtitleText, activeCountText, cancelledCountText, totalCountText;
    private ImageView backButton;
    private Button activeTabButton, completedTabButton, cancelledTabButton;
    private String staffName;
    private String staffEmail;
    private String currentFilter = "ALL"; // ALL, ACTIVE, COMPLETED, CANCELLED
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modern_booking_list);
        
        // Get staff info from intent
        staffName = getIntent().getStringExtra("STAFF_NAME");
        staffEmail = getIntent().getStringExtra("STAFF_EMAIL");
        
        if (staffName == null || staffName.isEmpty()) {
            Toast.makeText(this, "Staff information not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupRecyclerView();
        loadStaffBookings();
    }
    
    private void initViews() {
        titleText = findViewById(R.id.titleText);
        subtitleText = findViewById(R.id.subtitleText);
        backButton = findViewById(R.id.backButton);
        bookingRecyclerView = findViewById(R.id.modernBookingRecyclerView);
        activeCountText = findViewById(R.id.activeCountText);
        cancelledCountText = findViewById(R.id.cancelledCountText);
        totalCountText = findViewById(R.id.totalCountText);
        activeTabButton = findViewById(R.id.activeTabButton);
        completedTabButton = findViewById(R.id.completedTabButton);
        cancelledTabButton = findViewById(R.id.cancelledTabButton);
        
        // Update title for staff view
        titleText.setText("Customer Bookings");
        subtitleText.setText("Bookings made by users who selected " + staffName);
        
        // Update filter button text
        activeTabButton.setText("New Orders");
        
        // Set back button listener
        backButton.setOnClickListener(v -> finish());
        
        // Setup filter tabs
        setupFilterTabs();
    }
    
    private void setupFilterTabs() {
        activeTabButton.setOnClickListener(v -> filterBookings("ACTIVE"));
        completedTabButton.setOnClickListener(v -> filterBookings("COMPLETED"));
        cancelledTabButton.setOnClickListener(v -> filterBookings("CANCELLED"));
    }
    
    private void filterBookings(String status) {
        currentFilter = status;
        
        // Update button states
        updateFilterButtonStates();
        
        // Filter bookings
        if (allBookingsList == null || allBookingsList.isEmpty()) {
            return;
        }
        
        bookingList.clear();
        if ("ALL".equals(status)) {
            bookingList.addAll(allBookingsList);
        } else {
            for (Booking booking : allBookingsList) {
                if (status.equals(booking.getStatus())) {
                    bookingList.add(booking);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
    }
    
    private void updateFilterButtonStates() {
        // Reset all buttons
        activeTabButton.setBackgroundResource(R.drawable.button_secondary);
        activeTabButton.setTextColor(getResources().getColor(R.color.text_primary));
        completedTabButton.setBackgroundResource(R.drawable.button_secondary);
        completedTabButton.setTextColor(getResources().getColor(R.color.text_primary));
        cancelledTabButton.setBackgroundResource(R.drawable.button_secondary);
        cancelledTabButton.setTextColor(getResources().getColor(R.color.text_primary));
        
        // Highlight active filter
        switch (currentFilter) {
            case "ACTIVE":
                activeTabButton.setBackgroundResource(R.drawable.button_primary);
                activeTabButton.setTextColor(getResources().getColor(R.color.text_white));
                break;
            case "COMPLETED":
                completedTabButton.setBackgroundResource(R.drawable.button_primary);
                completedTabButton.setTextColor(getResources().getColor(R.color.text_white));
                break;
            case "CANCELLED":
                cancelledTabButton.setBackgroundResource(R.drawable.button_primary);
                cancelledTabButton.setTextColor(getResources().getColor(R.color.text_white));
                break;
        }
    }
    
    private void setupRecyclerView() {
        bookingList = new ArrayList<>();
        adapter = new ModernBookingAdapter(this, bookingList);
        adapter.setUserView(false); // Staff view - show customer info
        
        bookingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookingRecyclerView.setAdapter(adapter);
        
        // Set booking action listener
        adapter.setOnBookingActionListener((booking, position) -> {
            // Handle booking actions (e.g., mark as completed, cancel, etc.)
            showBookingActionDialog(booking, position);
        });
    }
    
    private void loadStaffBookings() {
        new Thread(() -> {
            try {
                // Load bookings by staff name (primary method)
                List<Booking> bookings = ConnectionClass.getBookingsByStaffName(staffName);
                
                // If no bookings found by name and we have email, try by email as fallback
                if ((bookings == null || bookings.isEmpty()) && staffEmail != null && !staffEmail.isEmpty()) {
                    bookings = ConnectionClass.getBookingsByStaffEmail(staffEmail);
                }
                
                // Make final reference for lambda
                final List<Booking> finalBookings = bookings;
                
                runOnUiThread(() -> {
                    if (finalBookings != null && !finalBookings.isEmpty()) {
                        // Store all bookings for filtering
                        allBookingsList = new ArrayList<>(finalBookings);
                        
                        // Display all bookings by default
                        bookingList.clear();
                        bookingList.addAll(allBookingsList);
                        adapter.notifyDataSetChanged();
                        
                        // Calculate stats
                        int activeCount = 0;
                        int cancelledCount = 0;
                        for (Booking booking : finalBookings) {
                            String status = booking.getStatus();
                            if (status != null) {
                                if ("ACTIVE".equals(status)) {
                                    activeCount++;
                                } else if ("CANCELLED".equals(status)) {
                                    cancelledCount++;
                                }
                            }
                        }
                        
                        // Update stats
                        activeCountText.setText(String.valueOf(activeCount));
                        cancelledCountText.setText(String.valueOf(cancelledCount));
                        totalCountText.setText(String.valueOf(finalBookings.size()));
                        
                        // Update subtitle with booking count
                        subtitleText.setText(finalBookings.size() + " bookings from users who selected " + staffName);
                        
                        // Set default filter to show all (New Orders/Active)
                        currentFilter = "ACTIVE";
                        updateFilterButtonStates();
                        filterBookings("ACTIVE");
                    } else {
                        allBookingsList = new ArrayList<>();
                        bookingList.clear();
                        adapter.notifyDataSetChanged();
                        
                        // Reset stats
                        activeCountText.setText("0");
                        cancelledCountText.setText("0");
                        totalCountText.setText("0");
                        subtitleText.setText("No bookings found for " + staffName);
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(StaffBookingActivity.this, "Error loading bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void showBookingActionDialog(Booking booking, int position) {
        // Create action dialog for staff to manage bookings
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Booking Actions");
        builder.setMessage("Service: " + booking.getServiceName() + "\n" +
                          "Customer: " + (booking.getCustomerName() != null && !booking.getCustomerName().isEmpty() 
                                        ? booking.getCustomerName() : booking.getCustomerEmail()) + "\n" +
                          "Time: " + booking.getBookingDate() + " " + booking.getBookingTime() + "\n" +
                          "Status: " + booking.getStatus());
        
        // Add action buttons based on current status
        String currentStatus = booking.getStatus();
        if ("ACTIVE".equals(currentStatus)) {
            builder.setPositiveButton("Mark Completed", (dialog, which) -> {
                updateBookingStatus(booking, "COMPLETED", position);
            });
            builder.setNegativeButton("Cancel Booking", (dialog, which) -> {
                updateBookingStatus(booking, "CANCELLED", position);
            });
        } else if ("COMPLETED".equals(currentStatus)) {
            builder.setPositiveButton("Mark Active", (dialog, which) -> {
                updateBookingStatus(booking, "ACTIVE", position);
            });
        } else if ("CANCELLED".equals(currentStatus)) {
            builder.setPositiveButton("Reactivate", (dialog, which) -> {
                updateBookingStatus(booking, "ACTIVE", position);
            });
        }
        
        builder.setNeutralButton("Close", null);
        builder.show();
    }
    
    private void updateBookingStatus(Booking booking, String newStatus, int position) {
        new Thread(() -> {
            try {
                // Update booking status with staff assignment
                boolean success = ConnectionClass.updateBookingStatusWithStaff(booking.getId(), newStatus, staffEmail);
                
                runOnUiThread(() -> {
                    if (success) {
                        // Update booking in both lists
                        booking.setStatus(newStatus);
                        if (allBookingsList != null) {
                            for (Booking b : allBookingsList) {
                                if (b.getId() == booking.getId()) {
                                    b.setStatus(newStatus);
                                    break;
                                }
                            }
                        }
                        
                        // Reapply current filter
                        filterBookings(currentFilter);
                        
                        // Recalculate and update stats from allBookingsList
                        if (allBookingsList != null) {
                            int activeCount = 0;
                            int cancelledCount = 0;
                            for (Booking b : allBookingsList) {
                                String status = b.getStatus();
                                if (status != null) {
                                    if ("ACTIVE".equals(status)) {
                                        activeCount++;
                                    } else if ("CANCELLED".equals(status)) {
                                        cancelledCount++;
                                    }
                                }
                            }
                            activeCountText.setText(String.valueOf(activeCount));
                            cancelledCountText.setText(String.valueOf(cancelledCount));
                            totalCountText.setText(String.valueOf(allBookingsList.size()));
                        }
                        
                        Toast.makeText(this, "Booking status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to update booking status", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error updating booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh bookings when returning to this activity
        loadStaffBookings();
    }
}
