package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mizrahbeauty.adapters.AdminServiceAdapter;
import com.example.mizrahbeauty.models.Service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminServicesByCategoryActivity extends AppCompatActivity {
    private RecyclerView servicesRecyclerView;
    private AdminServiceAdapter serviceAdapter;
    private List<Service> serviceList;
    private ExecutorService executorService;
    private Handler mainHandler;
    private String categoryName;
    private String adminEmail, adminName;
    private TextView titleText, serviceCountText;
    private ImageView backButton, addServiceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_services_by_category);

        // Get data from intent
        categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        adminEmail = getIntent().getStringExtra("USER_EMAIL");
        adminName = getIntent().getStringExtra("USER_NAME");

        initializeViews();
        setupClickListeners();
        setupRecyclerView();
        loadServices();
    }

    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        serviceCountText = findViewById(R.id.serviceCountText);
        backButton = findViewById(R.id.backButton);
        addServiceButton = findViewById(R.id.addServiceButton);
        servicesRecyclerView = findViewById(R.id.servicesRecyclerView);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Set title
        titleText.setText(categoryName);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        addServiceButton.setOnClickListener(v -> {
            // Navigate to add service page with category pre-filled
            Intent intent = new Intent(AdminServicesByCategoryActivity.this, AddServiceActivity.class);
            intent.putExtra("CATEGORY_NAME", categoryName);
            intent.putExtra("USER_EMAIL", adminEmail);
            intent.putExtra("USER_NAME", adminName);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        serviceList = new ArrayList<>();
        serviceAdapter = new AdminServiceAdapter(this, serviceList, new AdminServiceAdapter.OnServiceActionListener() {
            @Override
            public void onEditService(Service service, int position) {
                // Navigate to edit service
                Intent intent = new Intent(AdminServicesByCategoryActivity.this, EditServiceActivity.class);
                intent.putExtra("SERVICE_ID", service.getId());
                intent.putExtra("SERVICE_NAME", service.getServiceName());
                intent.putExtra("SERVICE_PRICE", service.getPrice());
                intent.putExtra("SERVICE_DURATION", service.getDurationMinutes());
                intent.putExtra("SERVICE_DETAILS", service.getDetails());
                intent.putExtra("SERVICE_CATEGORY", service.getCategory());
                intent.putExtra("USER_EMAIL", adminEmail);
                intent.putExtra("USER_NAME", adminName);
                startActivity(intent);
            }

            @Override
            public void onDeleteService(Service service, int position) {
                // Show confirmation dialog
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(AdminServicesByCategoryActivity.this);
                builder.setTitle("Delete Service")
                        .setMessage("Are you sure you want to delete service '" + service.getServiceName() + "'?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deleteService(service);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            
            @Override
            public void onServiceClick(Service service, int position) {
                // Show service description dialog
                showServiceDescriptionDialog(service);
            }
        });
        servicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        servicesRecyclerView.setAdapter(serviceAdapter);
    }

    private void loadServices() {
        executorService.execute(() -> {
            try {
                ResultSet rs = ConnectionClass.getServicesByCategory(categoryName);
                List<Service> newServiceList = new ArrayList<>();

                if (rs != null) {
                    while (rs.next()) {
                        Service service = new Service();
                        service.setId(rs.getInt("id"));
                        service.setCategory(rs.getString("category"));
                        service.setServiceName(rs.getString("service_name"));
                        service.setPrice(rs.getDouble("price"));
                        service.setDurationMinutes(rs.getInt("duration_minutes"));
                        service.setDetails(rs.getString("details"));
                        newServiceList.add(service);
                    }
                }

                mainHandler.post(() -> {
                    serviceList.clear();
                    serviceList.addAll(newServiceList);
                    serviceAdapter.notifyDataSetChanged();
                    updateServiceCount();
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    Toast.makeText(this, "Failed to load services", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void deleteService(Service service) {
        executorService.execute(() -> {
            boolean success = ConnectionClass.deleteService(service.getId());
            mainHandler.post(() -> {
                if (success) {
                    Toast.makeText(this, "Service deleted successfully", Toast.LENGTH_SHORT).show();
                    loadServices(); // Reload the list
                } else {
                    Toast.makeText(this, "Failed to delete service", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateServiceCount() {
        int count = serviceList.size();
        serviceCountText.setText(count + " services");
    }
    
    private void showServiceDescriptionDialog(Service service) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(service.getServiceName());
        
        // Create dialog content with full service details
        StringBuilder dialogContent = new StringBuilder();
        dialogContent.append("Category: ").append(service.getCategory() != null ? service.getCategory() : "N/A").append("\n\n");
        dialogContent.append("Duration: ").append(formatDuration(service.getDurationMinutes())).append("\n\n");
        dialogContent.append("Price: ").append(formatPrice(service.getPrice())).append("\n\n");
        dialogContent.append("Description:\n");
        
        String details = service.getDetails();
        if (details != null && !details.trim().isEmpty()) {
            dialogContent.append(details);
        } else {
            dialogContent.append("No description available");
        }
        
        builder.setMessage(dialogContent.toString());
        builder.setPositiveButton("Edit", (dialog, which) -> {
            // Navigate to edit service
            Intent intent = new Intent(AdminServicesByCategoryActivity.this, EditServiceActivity.class);
            intent.putExtra("SERVICE_ID", service.getId());
            intent.putExtra("SERVICE_NAME", service.getServiceName());
            intent.putExtra("SERVICE_PRICE", service.getPrice());
            intent.putExtra("SERVICE_DURATION", service.getDurationMinutes());
            intent.putExtra("SERVICE_DETAILS", service.getDetails());
            intent.putExtra("SERVICE_CATEGORY", service.getCategory());
            intent.putExtra("USER_EMAIL", adminEmail);
            intent.putExtra("USER_NAME", adminName);
            startActivity(intent);
        });
        builder.setNegativeButton("Close", null);
        
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
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
        java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("ms", "MY"));
        return formatter.format(price);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from other activities
        loadServices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
