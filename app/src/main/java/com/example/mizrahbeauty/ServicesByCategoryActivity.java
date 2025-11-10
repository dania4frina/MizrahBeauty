package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mizrahbeauty.adapters.ModernServiceAdapter;
import com.example.mizrahbeauty.models.Service;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServicesByCategoryActivity extends AppCompatActivity {
    
    private RecyclerView servicesRecyclerView;
    private ModernServiceAdapter serviceAdapter;
    private List<Service> serviceList;
    private ExecutorService executorService;
    private Handler mainHandler;
    private ImageView backButton;
    private TextView categoryNameText, serviceCountText;
    private String categoryName, userEmail;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services_by_category);
        
        // Get data from intent
        Intent intent = getIntent();
        categoryName = intent.getStringExtra("CATEGORY_NAME");
        userEmail = intent.getStringExtra("USER_EMAIL");
        
        // Initialize executor and handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        initializeViews();
        loadServicesByCategory();
    }
    
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        categoryNameText = findViewById(R.id.categoryNameText);
        serviceCountText = findViewById(R.id.serviceCountText);
        servicesRecyclerView = findViewById(R.id.servicesRecyclerView);
        
        // Set category name
        categoryNameText.setText(categoryName);
        
        // Setup back button
        backButton.setOnClickListener(v -> finish());
        
        // Setup RecyclerView
        servicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        serviceList = new ArrayList<>();
        serviceAdapter = new ModernServiceAdapter(this, serviceList, new ModernServiceAdapter.OnServiceActionListener() {
            @Override
            public void onBookService(Service service, int position) {
                // Navigate to booking page when book button is clicked
                Intent i = new Intent(ServicesByCategoryActivity.this, CreateBookingActivity.class);
                i.putExtra("SERVICE_ID", service.getId());
                i.putExtra("SERVICE_NAME", service.getServiceName());
                i.putExtra("SERVICE_PRICE", service.getPrice());
                i.putExtra("SERVICE_DURATION", service.getDurationMinutes());
                i.putExtra("USER_EMAIL", userEmail);
                startActivity(i);
            }
            
            @Override
            public void onServiceClick(Service service, int position) {
                // Show service description dialog when item is clicked
                showServiceDescriptionDialog(service);
            }
        });
        servicesRecyclerView.setAdapter(serviceAdapter);
    }
    
    private void showServiceDescriptionDialog(Service service) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(service.getServiceName());
        
        // Create dialog content
        String dialogContent = "Category: " + service.getCategory() + "\n\n" +
                              "Duration: " + service.getFormattedDuration() + "\n\n" +
                              "Price: " + service.getFormattedPrice() + "\n\n" +
                              "Description:\n" + 
                              (service.getDetails() != null && !service.getDetails().isEmpty() 
                                  ? service.getDetails() 
                                  : "No description available");
        
        builder.setMessage(dialogContent);
        builder.setPositiveButton("Book Now", (dialog, which) -> {
            // Navigate to booking page
            Intent i = new Intent(ServicesByCategoryActivity.this, CreateBookingActivity.class);
            i.putExtra("SERVICE_ID", service.getId());
            i.putExtra("SERVICE_NAME", service.getServiceName());
            i.putExtra("SERVICE_PRICE", service.getPrice());
            i.putExtra("SERVICE_DURATION", service.getDurationMinutes());
            i.putExtra("USER_EMAIL", userEmail);
            startActivity(i);
        });
        builder.setNegativeButton("Close", null);
        
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void loadServicesByCategory() {
        executorService.execute(() -> {
            try {
                // Load services for this category from database
                ResultSet rs = ConnectionClass.getServicesByCategory(categoryName);
                List<Service> services = new ArrayList<>();
                
                if (rs != null) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String category = rs.getString("category");
                        String serviceName = rs.getString("service_name");
                        double price = rs.getDouble("price");
                        int duration = rs.getInt("duration_minutes");
                        String details = rs.getString("details");
                        
                        Service service = new Service(id, category, serviceName, price, duration, details);
                        services.add(service);
                    }
                    rs.close();
                }
                
                // Update UI on main thread
                mainHandler.post(() -> {
                    serviceList.clear();
                    serviceList.addAll(services);
                    serviceAdapter.notifyDataSetChanged();
                    
                    // Update service count
                    serviceCountText.setText(services.size() + " Service" + (services.size() > 1 ? "s" : ""));
                    
                    if (services.isEmpty()) {
                        Toast.makeText(ServicesByCategoryActivity.this, 
                            "No services found in this category", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    Toast.makeText(ServicesByCategoryActivity.this, 
                        "Error loading services: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
}

