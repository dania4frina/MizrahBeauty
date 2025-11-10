package com.example.mizrahbeauty;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class StaffManagementActivity extends AppCompatActivity {
    
    private RecyclerView staffRecyclerView;
    private StaffAdapter staffAdapter;
    private List<StaffMember> staffList;
    private ExecutorService executorService;
    private ImageView backButton;
    private TextView titleText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_management);
        
        executorService = Executors.newSingleThreadExecutor();
        
        initializeViews();
        setupRecyclerView();
        loadStaffList();
    }
    
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);
        staffRecyclerView = findViewById(R.id.staffRecyclerView);
        
        titleText.setText("Staff Management");
        
        backButton.setOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        staffList = new ArrayList<>();
        staffAdapter = new StaffAdapter(staffList, this::showDeleteConfirmation);
        staffRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        staffRecyclerView.setAdapter(staffAdapter);
    }
    
    private void loadStaffList() {
        executorService.execute(() -> {
            try {
                ResultSet rs = ConnectionClass.getAllStaff();
                List<StaffMember> staff = new ArrayList<>();
                
                if (rs != null) {
                    while (rs.next()) {
                        String email = rs.getString("user_email");
                        String name = rs.getString("name");
                        String role = "staff"; // Default role since we're getting from staff table
                        
                        staff.add(new StaffMember(email, name, role));
                    }
                    rs.close();
                }
                
                runOnUiThread(() -> {
                    staffList.clear();
                    staffList.addAll(staff);
                    staffAdapter.notifyDataSetChanged();
                    
                    Toast.makeText(this, "Loaded " + staff.size() + " staff members", 
                        Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading staff: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void showDeleteConfirmation(StaffMember staff) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Staff")
                .setMessage("Are you sure you want to delete " + staff.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteStaff(staff))
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void deleteStaff(StaffMember staff) {
        executorService.execute(() -> {
            try {
                boolean success = ConnectionClass.deleteStaff(staff.getEmail());
                
                runOnUiThread(() -> {
                    if (success) {
                        staffList.remove(staff);
                        staffAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Staff deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to delete staff", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    
    // Staff Member Model
    static class StaffMember {
        private String email;
        private String name;
        private String role;
        
        public StaffMember(String email, String name, String role) {
            this.email = email;
            this.name = name;
            this.role = role;
        }
        
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getRole() { return role; }
    }
    
    // Staff Adapter
    static class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {
        private List<StaffMember> staffList;
        private OnDeleteClickListener deleteListener;
        
        interface OnDeleteClickListener {
            void onDeleteClick(StaffMember staff);
        }
        
        public StaffAdapter(List<StaffMember> staffList, OnDeleteClickListener deleteListener) {
            this.staffList = staffList;
            this.deleteListener = deleteListener;
        }
        
        @NonNull
        @Override
        public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_staff_member, parent, false);
            return new StaffViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
            StaffMember staff = staffList.get(position);
            
            holder.nameText.setText(staff.getName());
            holder.emailText.setText(staff.getEmail());
            holder.roleText.setText(staff.getRole());
            
            holder.deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(staff);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return staffList.size();
        }
        
        static class StaffViewHolder extends RecyclerView.ViewHolder {
            TextView nameText, emailText, roleText;
            ImageView deleteButton;
            
            public StaffViewHolder(@NonNull View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.staffNameText);
                emailText = itemView.findViewById(R.id.staffEmailText);
                roleText = itemView.findViewById(R.id.staffRoleText);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }
}

