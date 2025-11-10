package com.example.mizrahbeauty;

import android.app.AlertDialog;
import android.content.Intent;
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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModernBookingListActivity extends AppCompatActivity implements ModernBookingAdapter.OnBookingActionListener {
    
    private TextView titleText, subtitleText, activeCountText, cancelledCountText, totalCountText;
    private ImageView backButton;
    private Button activeTabButton, completedTabButton, cancelledTabButton;
    private RecyclerView modernBookingRecyclerView;
    private ModernBookingAdapter modernBookingAdapter;
    private List<Booking> bookingList;
    private List<Booking> allBookingList; // Store all bookings
    private ExecutorService executorService;
    private String userEmail, userRole;
    private String currentFilter = "active"; // active, completed, cancelled
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modern_booking_list);
        
        // Get data from intent
        Intent intent = getIntent();
        userEmail = intent.getStringExtra("USER_EMAIL");
        userRole = intent.getStringExtra("USER_ROLE");
        
        executorService = Executors.newSingleThreadExecutor();
        initializeViews();
        setupClickListeners();
        updateTabButtons(); // Set initial tab state
        loadBookings();
    }
    
    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        subtitleText = findViewById(R.id.subtitleText);
        activeCountText = findViewById(R.id.activeCountText);
        cancelledCountText = findViewById(R.id.cancelledCountText);
        totalCountText = findViewById(R.id.totalCountText);
        backButton = findViewById(R.id.backButton);
        activeTabButton = findViewById(R.id.activeTabButton);
        completedTabButton = findViewById(R.id.completedTabButton);
        cancelledTabButton = findViewById(R.id.cancelledTabButton);
        modernBookingRecyclerView = findViewById(R.id.modernBookingRecyclerView);
        
        // Customize UI based on user role
        if ("user".equals(userRole)) {
            titleText.setText("My Orders");
            subtitleText.setText("Your booking history");
        } else {
            titleText.setText("Bookings");
            subtitleText.setText("Manage your appointments");
        }
        
        // Initialize booking lists
        bookingList = new ArrayList<>();
        allBookingList = new ArrayList<>();
        
        // Setup RecyclerView
        modernBookingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        modernBookingAdapter = new ModernBookingAdapter(this, bookingList, this);
        modernBookingAdapter.setUserView("user".equals(userRole)); // Hide customer email for user view
        modernBookingRecyclerView.setAdapter(modernBookingAdapter);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        activeTabButton.setOnClickListener(v -> {
            currentFilter = "active";
            updateTabButtons();
            filterBookings();
        });
        
        completedTabButton.setOnClickListener(v -> {
            currentFilter = "completed";
            updateTabButtons();
            filterBookings();
        });
        
        cancelledTabButton.setOnClickListener(v -> {
            currentFilter = "cancelled";
            updateTabButtons();
            filterBookings();
        });
    }
    
    private void loadBookings() {
        executorService.execute(() -> {
            try {
                System.out.println("=== LOADING BOOKINGS DEBUG ===");
                System.out.println("User role: " + userRole);
                System.out.println("User email: " + userEmail);
                
                // Load bookings based on user role
                ResultSet rs;
                if ("user".equals(userRole)) {
                    // Load only user's bookings
                    System.out.println("Loading user bookings for: " + userEmail);
                    rs = ConnectionClass.getUserBookings(userEmail);
                } else {
                    // Load all bookings for staff
                    System.out.println("Loading all bookings for staff");
                    rs = ConnectionClass.getAllBookingsForStaff();
                }
                
                System.out.println("ResultSet is null: " + (rs == null));
                
                List<Booking> bookings = new ArrayList<>();
                int activeCount = 0;
                int cancelledCount = 0;
                
                if (rs != null) {
                    int rowCount = 0;
                    while (rs.next()) {
                        rowCount++;
                        System.out.println("Processing row " + rowCount);
                        
                        String status = rs.getString("status");
                        if (status == null) status = "UNKNOWN"; // Default status if null
                        
                        // Parse appointment_time to separate date and time
                        String appointmentTime = rs.getString("appointment_time");
                        String[] dateTimeParts = appointmentTime != null ? appointmentTime.split(" ") : new String[]{"", ""};
                        String bookingDate = dateTimeParts.length > 0 ? dateTimeParts[0] : "";
                        String bookingTime = dateTimeParts.length > 1 ? dateTimeParts[1] : "";
                        
                        // Get staff name or use fallback
                        String staffName = rs.getString("staff_name");
                        if (staffName == null || staffName.isEmpty()) {
                            staffName = "Unknown";
                        }

                        String customerEmail = rs.getString("user_email");
                        String customerName = rs.getString("customer_name");
                        if (customerName == null || customerName.trim().isEmpty()) {
                            customerName = customerEmail != null ? customerEmail : "Unknown";
                        }
                        
                        System.out.println("Booking ID: " + rs.getInt("id") + 
                                         ", Service: " + rs.getString("service_name") + 
                                         ", Status: " + status + 
                                         ", Staff: " + staffName);
                        
                        // Get notes from database
                        String notes = rs.getString("notes");
                        if (notes == null) notes = "";
                        
                        Booking booking = new Booking(
                                rs.getInt("id"),
                                rs.getString("service_name"),
                                staffName,
                                bookingDate,
                                bookingTime,
                                status,
                                notes,
                                customerName,
                                customerEmail
                        );
                        bookings.add(booking);
                        
                        if ("ACTIVE".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status)) {
                            activeCount++;
                        } else if ("CANCELLED".equalsIgnoreCase(status)) {
                            cancelledCount++;
                        }
                    }
                    rs.close();
                    System.out.println("Total rows processed: " + rowCount);
                } else {
                    System.out.println("ResultSet is null - no data returned from database");
                }
                
                final int finalActiveCount = activeCount;
                final int finalCancelledCount = cancelledCount;
                final int totalCount = bookings.size();
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    allBookingList.clear();
                    allBookingList.addAll(bookings);
                    
                    // Update stats
                    activeCountText.setText(String.valueOf(finalActiveCount));
                    cancelledCountText.setText(String.valueOf(finalCancelledCount));
                    totalCountText.setText(String.valueOf(totalCount));
                    
                    // Filter bookings based on current filter
                    filterBookings();
                    
                    Toast.makeText(ModernBookingListActivity.this, 
                        "Loaded " + bookings.size() + " bookings", 
                        Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(ModernBookingListActivity.this, 
                        "Error loading bookings: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    @Override
    public void onBookingAction(Booking booking, int position) {
        // Show action dialog based on user role
        String[] actions;
        if ("user".equals(userRole)) {
            actions = new String[]{"View Details", "Cancel Booking"};
        } else {
            actions = new String[]{"View Details", "Cancel Booking", "Mark Complete"};
        }
        
        new AlertDialog.Builder(this)
                .setTitle(booking.getServiceName())
                .setItems(actions, (dialog, which) -> {
                    switch (which) {
                        case 0: // View Details
                            showBookingDetails(booking);
                            break;
                        case 1: // Cancel Booking
                            if (booking.getStatus() != null && "ACTIVE".equalsIgnoreCase(booking.getStatus())) {
                                // Allow cancellation for all users (simplified for current model)
                                cancelBooking(booking, position);
                            } else {
                                Toast.makeText(this, "Only active bookings can be cancelled", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 2: // Mark Complete (Staff only)
                            if (!"user".equals(userRole) && booking.getStatus() != null && "ACTIVE".equalsIgnoreCase(booking.getStatus())) {
                                completeBooking(booking, position);
                            } else if ("user".equals(userRole)) {
                                // This shouldn't happen but just in case
                                Toast.makeText(this, "Only staff can mark bookings as complete", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Only active bookings can be marked complete", Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                })
                .show();
    }
    
    private void showBookingDetails(Booking booking) {
        new AlertDialog.Builder(this)
                .setTitle("Booking Details")
                .setMessage("Service: " + (booking.getServiceName() != null ? booking.getServiceName() : "Unknown") + "\n" +
                        "Customer: " + (booking.getCustomerName() != null && !booking.getCustomerName().isEmpty() ? booking.getCustomerName() : "Unknown") + "\n" +
                        "Beautician: " + (booking.getStaffName() != null ? booking.getStaffName() : "Unknown") + "\n" +
                        "Date: " + (booking.getBookingDate() != null ? booking.getBookingDate() : "Unknown") + "\n" +
                        "Time: " + (booking.getBookingTime() != null ? booking.getBookingTime() : "Unknown") + "\n" +
                        "Status: " + (booking.getStatus() != null ? booking.getStatus() : "Unknown"))
                .setPositiveButton("OK", null)
                .show();
    }
    
    private void cancelBooking(Booking booking, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    updateBookingStatus(booking, position, "CANCELLED");
                })
                .setNegativeButton("No", null)
                .show();
    }
    
    private void completeBooking(Booking booking, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Complete Booking")
                .setMessage("Mark this booking as completed?")
                .setPositiveButton("Yes, Complete", (dialog, which) -> {
                    updateBookingStatus(booking, position, "COMPLETED");
                })
                .setNegativeButton("No", null)
                .show();
    }
    
    private void updateBookingStatus(Booking booking, int position, String newStatus) {
        executorService.execute(() -> {
            try {
                System.out.println("=== UPDATE BOOKING STATUS DEBUG ===");
                System.out.println("User Role: " + userRole);
                System.out.println("User Email: " + userEmail);
                System.out.println("New Status: " + newStatus);
                System.out.println("Booking ID: " + booking.getBookingId());
                
                boolean success;
                if ("user".equals(userRole) && "CANCELLED".equals(newStatus)) {
                    // Use user-specific cancel method
                    System.out.println("Using cancelBooking method");
                    success = ConnectionClass.cancelBooking(booking.getBookingId(), userEmail);
                } else if ("staff".equals(userRole) && "COMPLETED".equals(newStatus)) {
                    // When staff completes booking, assign staff to booking
                    System.out.println("Using updateBookingStatusWithStaff method");
                    System.out.println("Assigning staff email: " + userEmail);
                    success = ConnectionClass.updateBookingStatusWithStaff(booking.getBookingId(), newStatus, userEmail);
                } else {
                    // Use general update method
                    System.out.println("Using general updateBookingStatus method");
                    success = ConnectionClass.updateBookingStatus(booking.getBookingId(), newStatus);
                }
                
                runOnUiThread(() -> {
                    if (success) {
                        booking.setStatus(newStatus);
                        modernBookingAdapter.notifyItemChanged(position);
                        loadBookings(); // Refresh stats
                        
                        String message = "CANCELLED".equals(newStatus) ? "Booking cancelled successfully" : 
                                        "Booking " + (newStatus != null ? newStatus.toLowerCase() : "updated") + " successfully";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
        });
    }
    
    private void filterBookings() {
        bookingList.clear();
        
        for (Booking booking : allBookingList) {
            String status = booking.getStatus().toUpperCase();
            
            if ("active".equals(currentFilter)) {
                // Show active bookings (ACTIVE, PENDING, CONFIRMED)
                if ("ACTIVE".equals(status) || "PENDING".equals(status) || "CONFIRMED".equals(status)) {
                    bookingList.add(booking);
                }
            } else if ("completed".equals(currentFilter)) {
                // Show completed bookings (COMPLETED only)
                if ("COMPLETED".equals(status)) {
                    bookingList.add(booking);
                }
            } else if ("cancelled".equals(currentFilter)) {
                // Show cancelled bookings (CANCELLED only)
                if ("CANCELLED".equals(status)) {
                    bookingList.add(booking);
                }
            }
        }
        
        modernBookingAdapter.notifyDataSetChanged();
    }
    
    private void updateTabButtons() {
        // Reset all buttons to secondary style
        activeTabButton.setBackgroundResource(R.drawable.button_secondary);
        completedTabButton.setBackgroundResource(R.drawable.button_secondary);
        cancelledTabButton.setBackgroundResource(R.drawable.button_secondary);
        activeTabButton.setTextColor(getResources().getColor(R.color.text_primary));
        completedTabButton.setTextColor(getResources().getColor(R.color.text_primary));
        cancelledTabButton.setTextColor(getResources().getColor(R.color.text_primary));
        
        // Set active button to primary style
        if ("active".equals(currentFilter)) {
            activeTabButton.setBackgroundResource(R.drawable.button_primary);
            activeTabButton.setTextColor(getResources().getColor(R.color.text_white));
        } else if ("completed".equals(currentFilter)) {
            completedTabButton.setBackgroundResource(R.drawable.button_primary);
            completedTabButton.setTextColor(getResources().getColor(R.color.text_white));
        } else if ("cancelled".equals(currentFilter)) {
            cancelledTabButton.setBackgroundResource(R.drawable.button_primary);
            cancelledTabButton.setTextColor(getResources().getColor(R.color.text_white));
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
