package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StaffStatusPreviewActivity extends AppCompatActivity {
    
    private RecyclerView staffRecyclerView;
    private StaffStatusAdapter staffAdapter;
    private List<StaffStatus> staffList;
    private ExecutorService executorService;
    private ImageView backButton;
    private TextView titleText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_status_preview);
        
        executorService = Executors.newSingleThreadExecutor();
        
        initializeViews();
        setupRecyclerView();
        loadStaffStatus();
    }
    
    private void initializeViews() {
        try {
            backButton = findViewById(R.id.backButton);
            titleText = findViewById(R.id.titleText);
            staffRecyclerView = findViewById(R.id.staffRecyclerView);
            
            titleText.setText("Staff Status Preview");
            
            backButton.setOnClickListener(v -> finish());
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupRecyclerView() {
        staffList = new ArrayList<>();
        staffAdapter = new StaffStatusAdapter(staffList);
        staffRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        staffRecyclerView.setAdapter(staffAdapter);
    }
    
    private void loadStaffStatus() {
        executorService.execute(() -> {
            try {
                ResultSet rs = ConnectionClass.getAllStaff();
                List<StaffStatus> staff = new ArrayList<>();
                
                if (rs != null) {
                    while (rs.next()) {
                        try {
                            String email = rs.getString("user_email");
                            String name = rs.getString("name");
                            String phone = rs.getString("phone");
                            boolean isAvailable = rs.getBoolean("is_available");
                            boolean isActive = rs.getBoolean("is_active");
                            
                            staff.add(new StaffStatus(email, name, phone, isAvailable, isActive));
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Skip this staff if there's an error
                        }
                    }
                    rs.close();
                }
                
                runOnUiThread(() -> {
                    staffList.clear();
                    staffList.addAll(staff);
                    staffAdapter.notifyDataSetChanged();
                    
                    if (staff.isEmpty()) {
                        Toast.makeText(this, "No staff found", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Loaded " + staff.size() + " staff members", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading staff: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
    
    // Staff Status Model
    static class StaffStatus {
        private String email;
        private String name;
        private String phone;
        private boolean isAvailable;
        private boolean isActive;
        
        public StaffStatus(String email, String name, String phone, boolean isAvailable, boolean isActive) {
            this.email = email;
            this.name = name;
            this.phone = phone;
            this.isAvailable = isAvailable;
            this.isActive = isActive;
        }
        
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getPhone() { return phone; }
        public boolean isAvailable() { return isAvailable; }
        public boolean isActive() { return isActive; }
    }
    
    // Staff Status Adapter
    static class StaffStatusAdapter extends RecyclerView.Adapter<StaffStatusAdapter.StaffStatusViewHolder> {
        private List<StaffStatus> staffList;
        
        public StaffStatusAdapter(List<StaffStatus> staffList) {
            this.staffList = staffList;
        }
        
        @NonNull
        @Override
        public StaffStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_staff_status, parent, false);
            return new StaffStatusViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull StaffStatusViewHolder holder, int position) {
            StaffStatus staff = staffList.get(position);
            
            holder.staffInfoText.setText(staff.getEmail() + 
                (staff.getName() != null ? " | " + staff.getName() : "") + 
                (staff.getPhone() != null ? " | " + staff.getPhone() : ""));
            
            // Set availability status
            if (staff.isActive()) {
                if (staff.isAvailable()) {
                    holder.availabilityStatusText.setText("Available");
                    holder.availabilityStatusText.setTextColor(0xFF4CAF50); // Green
                    holder.statusIndicator.setBackgroundColor(0xFF4CAF50); // Green
                } else {
                    holder.availabilityStatusText.setText("Busy");
                    holder.availabilityStatusText.setTextColor(0xFFFF9800); // Orange
                    holder.statusIndicator.setBackgroundColor(0xFFFF9800); // Orange
                }
            } else {
                holder.availabilityStatusText.setText("Inactive");
                holder.availabilityStatusText.setTextColor(0xFFF44336); // Red
                holder.statusIndicator.setBackgroundColor(0xFFF44336); // Red
            }
        }
        
        @Override
        public int getItemCount() {
            return staffList.size();
        }
        
        static class StaffStatusViewHolder extends RecyclerView.ViewHolder {
            TextView staffInfoText;
            TextView availabilityStatusText;
            View statusIndicator;
            
            public StaffStatusViewHolder(@NonNull View itemView) {
                super(itemView);
                staffInfoText = itemView.findViewById(R.id.staffInfoText);
                availabilityStatusText = itemView.findViewById(R.id.availabilityStatusText);
                statusIndicator = itemView.findViewById(R.id.statusIndicator);
            }
        }
    }
}
