package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServicesListActivity extends AppCompatActivity implements ModernServiceAdapter.OnServiceActionListener {
    
    private TextView titleText, subtitleText, serviceCountText, categoryCountText;
    private ImageView backButton;
    private RecyclerView modernServiceRecyclerView;
    private ModernServiceAdapter modernServiceAdapter;
    private List<Service> serviceList;
    private ExecutorService executorService;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modern_services);
        
        // Get user email from intent
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        
        executorService = Executors.newSingleThreadExecutor();
        initializeViews();
        setupClickListeners();
        loadServices();
    }
    
    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        subtitleText = findViewById(R.id.subtitleText);
        serviceCountText = findViewById(R.id.serviceCountText);
        categoryCountText = findViewById(R.id.categoryCountText);
        backButton = findViewById(R.id.backButton);
        modernServiceRecyclerView = findViewById(R.id.modernServiceRecyclerView);
        
        // Initialize service list
        serviceList = new ArrayList<>();
        
        // Setup RecyclerView
        modernServiceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        modernServiceAdapter = new ModernServiceAdapter(this, serviceList, this);
        modernServiceRecyclerView.setAdapter(modernServiceAdapter);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
    }
    
    private void loadServices() {
        executorService.execute(() -> {
            try {
                ResultSet rs = ConnectionClass.getAllServices();
                List<Service> services = new ArrayList<>();
                Set<String> categories = new HashSet<>();
                
                if (rs != null) {
                    while (rs.next()) {
                        Service service = new Service(
                            rs.getInt("id"),
                            rs.getString("category"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("duration_minutes"),
                            rs.getString("details")
                        );
                        services.add(service);
                        
                        // Collect unique categories
                        String category = service.getCategory();
                        if (category != null && !category.trim().isEmpty()) {
                            categories.add(category.trim());
                        }
                    }
                    rs.close();
                }
                
                final int serviceCount = services.size();
                final int categoryCount = categories.size();
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    serviceList.clear();
                    serviceList.addAll(services);
                    modernServiceAdapter.notifyDataSetChanged();
                    
                    // Update stats
                    serviceCountText.setText(String.valueOf(serviceCount));
                    categoryCountText.setText(String.valueOf(categoryCount));
                    
                    Toast.makeText(ServicesListActivity.this, 
                        "Loaded " + serviceCount + " services", 
                        Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(ServicesListActivity.this, 
                        "Error loading services: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    @Override
    public void onBookService(Service service, int position) {
        Intent intent = new Intent(ServicesListActivity.this, CreateBookingActivity.class);
        intent.putExtra("USER_EMAIL", userEmail);
        intent.putExtra("SERVICE_ID", service.getId());
        intent.putExtra("SERVICE_NAME", service.getServiceName());
        intent.putExtra("SERVICE_PRICE", service.getPrice());
        startActivity(intent);
    }
    
    @Override
    public void onServiceClick(Service service, int position) {
        // Show service description dialog when item is clicked
        showServiceDescriptionDialog(service);
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
            Intent intent = new Intent(ServicesListActivity.this, CreateBookingActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            intent.putExtra("SERVICE_ID", service.getId());
            startActivity(intent);
        });
        builder.setNegativeButton("Close", null);
        
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}


