package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mizrahbeauty.adapters.AdminCategoryListAdapter;
import com.example.mizrahbeauty.models.Category;
import com.example.mizrahbeauty.models.Service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminServiceDashboardActivity extends AppCompatActivity {
    private String adminEmail, adminName;
    private TextView titleText, categoryCountText;
    private ImageView backButton;
    private Button addServiceButton;
    private RecyclerView categoriesRecyclerView;
    private AdminCategoryListAdapter categoryAdapter;
    private List<Category> categoryList;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_service_dashboard);

        // Get admin info from intent
        adminEmail = getIntent().getStringExtra("USER_EMAIL");
        adminName = getIntent().getStringExtra("USER_NAME");

        initializeViews();
        setupClickListeners();
        setupRecyclerView();
        loadCategoriesAndServices();
    }

    private void initializeViews() {
        try {
            System.out.println("=== INITIALIZING VIEWS ===");
            titleText = findViewById(R.id.titleText);
            System.out.println("titleText initialized");
            
            categoryCountText = findViewById(R.id.categoryCountText);
            System.out.println("categoryCountText initialized");
            
            backButton = findViewById(R.id.backButton);
            System.out.println("backButton initialized");
            
            addServiceButton = findViewById(R.id.addServiceButton);
            System.out.println("addServiceButton initialized");
            
            categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
            System.out.println("categoriesRecyclerView initialized");

            executorService = Executors.newSingleThreadExecutor();
            mainHandler = new Handler(Looper.getMainLooper());

            // Set title
            titleText.setText(getString(R.string.service_management));
            System.out.println("Views initialized successfully");
        } catch (Exception e) {
            System.out.println("Error initializing views: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        addServiceButton.setOnClickListener(v -> {
            // Navigate to add service page
            Intent intent = new Intent(AdminServiceDashboardActivity.this, AddServiceActivity.class);
            intent.putExtra("USER_EMAIL", adminEmail);
            intent.putExtra("USER_NAME", adminName);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        try {
            System.out.println("=== SETTING UP RECYCLER VIEW ===");
            categoryList = new ArrayList<>();
            categoryAdapter = new AdminCategoryListAdapter(categoryList, new AdminCategoryListAdapter.OnCategoryClickListener() {
                @Override
                public void onCategoryClick(Category category) {
                    // Navigate to services by category for admin
                    Intent intent = new Intent(AdminServiceDashboardActivity.this, AdminServicesByCategoryActivity.class);
                    intent.putExtra("CATEGORY_NAME", category.getCategoryName());
                    intent.putExtra("USER_EMAIL", adminEmail);
                    intent.putExtra("USER_NAME", adminName);
                    startActivity(intent);
                }
            });
            categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            categoriesRecyclerView.setAdapter(categoryAdapter);

            // Set up add button
            addServiceButton.setOnClickListener(v -> {
                Intent intent = new Intent(AdminServiceDashboardActivity.this, AddServiceActivity.class);
                intent.putExtra("USER_EMAIL", adminEmail);
                intent.putExtra("USER_NAME", adminName);
                startActivity(intent);
            });
            System.out.println("RecyclerView setup completed successfully");
        } catch (Exception e) {
            System.out.println("Error setting up RecyclerView: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error setting up RecyclerView: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadCategoriesAndServices() {
        executorService.execute(() -> {
            try {
                ResultSet rs = ConnectionClass.getAllServices();
                Map<String, List<Service>> servicesByCategory = new HashMap<>();

                if (rs != null) {
                    while (rs.next()) {
                        Service service = new Service();
                        service.setId(rs.getInt("id"));
                        service.setCategory(rs.getString("category"));
                        service.setServiceName(rs.getString("service_name"));
                        service.setPrice(rs.getDouble("price"));
                        service.setDurationMinutes(rs.getInt("duration_minutes"));
                        service.setDetails(rs.getString("details"));

                        String category = service.getCategory();
                        if (!servicesByCategory.containsKey(category)) {
                            servicesByCategory.put(category, new ArrayList<>());
                        }
                        servicesByCategory.get(category).add(service);
                    }
                }

                // Convert to Category list
                List<Category> newCategoryList = new ArrayList<>();
                for (Map.Entry<String, List<Service>> entry : servicesByCategory.entrySet()) {
                    newCategoryList.add(new Category(entry.getKey(), entry.getValue()));
                }

                // Sort categories alphabetically (case-insensitive)
                newCategoryList.sort((c1, c2) -> c1.getCategoryName().compareToIgnoreCase(c2.getCategoryName()));

                mainHandler.post(() -> {
                    System.out.println("=== ADMIN SERVICE DASHBOARD DEBUG ===");
                    System.out.println("Total categories loaded: " + newCategoryList.size());
                    for (Category cat : newCategoryList) {
                        System.out.println("Category: " + cat.getCategoryName() + " - Services: " + cat.getServiceCount());
                    }
                    
                    categoryList.clear();
                    categoryList.addAll(newCategoryList);
                    categoryAdapter.notifyDataSetChanged();
                    updateCategoryCount();
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    Toast.makeText(this, "Failed to load services", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateCategoryCount() {
        int count = categoryList.size();
        categoryCountText.setText(count + " " + getString(R.string.categories).toLowerCase());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from other activities
        loadCategoriesAndServices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
