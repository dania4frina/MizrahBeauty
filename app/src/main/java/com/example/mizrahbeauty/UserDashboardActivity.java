package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mizrahbeauty.adapters.CategoryListAdapter;
import com.example.mizrahbeauty.models.Category;
import com.example.mizrahbeauty.models.Service;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserDashboardActivity extends AppCompatActivity {
    
    private RecyclerView categoriesRecyclerView;
    private CategoryListAdapter categoryAdapter;
    private LinearLayout historyNavItem, chatNavItem, profileNavItem, allFeedbackNavItem;
    private String userName, userEmail;
    private List<Category> categoryList;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);
        
        // Get user info from intent
        Intent intent = getIntent();
        userName = intent.getStringExtra("USER_NAME");
        userEmail = intent.getStringExtra("USER_EMAIL");
        
        // Initialize executor and handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        initializeViews();
        setupClickListeners();
        loadCategoriesAndServices();
    }
    
    private void initializeViews() {
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryListAdapter(this, categoryList, category -> {
            // Navigate to services by category page when category is clicked
            Intent i = new Intent(UserDashboardActivity.this, ServicesByCategoryActivity.class);
            i.putExtra("CATEGORY_NAME", category.getCategoryName());
            i.putExtra("USER_EMAIL", userEmail);
            startActivity(i);
        });
        categoriesRecyclerView.setAdapter(categoryAdapter);
        
        // Bottom navigation items
        historyNavItem = findViewById(R.id.historyNavItem);
        chatNavItem = findViewById(R.id.chatNavItem);
        allFeedbackNavItem = findViewById(R.id.allFeedbackNavItem);
        profileNavItem = findViewById(R.id.profileNavItem);
    }
    
    private void setupClickListeners() {
        // Bottom navigation click listeners
        historyNavItem.setOnClickListener(v -> {
            // Navigate to My Bookings (shows all bookings with status)
            Intent i = new Intent(UserDashboardActivity.this, ModernBookingListActivity.class);
            i.putExtra("USER_EMAIL", userEmail);
            i.putExtra("USER_ROLE", "user");
            startActivity(i);
        });
        
        chatNavItem.setOnClickListener(v -> {
            // Navigate to feedback page
            Intent i = new Intent(UserDashboardActivity.this, FeedbackActivity.class);
            i.putExtra("USER_EMAIL", userEmail);
            i.putExtra("USER_NAME", userName);
            startActivity(i);
        });
        
        allFeedbackNavItem.setOnClickListener(v -> {
            // Navigate to Public Feedback (All Reviews)
            Intent i = new Intent(UserDashboardActivity.this, PublicFeedbackActivity.class);
            startActivity(i);
        });
        
        profileNavItem.setOnClickListener(v -> {
            // Navigate to Profile
            Intent i = new Intent(UserDashboardActivity.this, ProfileActivity.class);
            i.putExtra("USER_EMAIL", userEmail);
            startActivity(i);
        });
    }
    
    private void loadCategoriesAndServices() {
        executorService.execute(() -> {
            try {
                // Load all services from database
                ResultSet rs = ConnectionClass.getAllServices();
                
                // Group services by category
                Map<String, List<Service>> categoryMap = new HashMap<>();
                
                if (rs != null) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String category = rs.getString("category");
                        String serviceName = rs.getString("service_name");
                        double price = rs.getDouble("price");
                        int duration = rs.getInt("duration_minutes");
                        String details = rs.getString("details");
                        
                        Service service = new Service(id, category, serviceName, price, duration, details);
                        
                        // Add to category map
                        if (!categoryMap.containsKey(category)) {
                            categoryMap.put(category, new ArrayList<>());
                        }
                        categoryMap.get(category).add(service);
                    }
                    rs.close();
                }
                
                // Create category objects with services
                List<Category> categories = new ArrayList<>();
                
                // Order categories as desired (case-insensitive)
                String[] categoryOrder = {"Basic Spa", "Face Relaxing", "Hair Salon", "Bridal Package"};
                
                // First, add categories in the predefined order
                for (String categoryName : categoryOrder) {
                    // Case-insensitive search
                    for (Map.Entry<String, List<Service>> entry : categoryMap.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(categoryName)) {
                            Category cat = new Category(entry.getKey(), entry.getValue());
                            categories.add(cat);
                            break;
                        }
                    }
                }
                
                // Add any remaining categories not in the predefined order
                for (Map.Entry<String, List<Service>> entry : categoryMap.entrySet()) {
                    boolean found = false;
                    for (Category cat : categories) {
                        if (cat.getCategoryName().equalsIgnoreCase(entry.getKey())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        categories.add(new Category(entry.getKey(), entry.getValue()));
                    }
                }
                
                // Update UI on main thread
                mainHandler.post(() -> {
                    categoryList.clear();
                    categoryList.addAll(categories);
                    categoryAdapter.notifyDataSetChanged();
                    
                    if (categories.isEmpty()) {
                        Toast.makeText(UserDashboardActivity.this, 
                            "No services available", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    Toast.makeText(UserDashboardActivity.this, 
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
    
    @Override
    public void onBackPressed() {
        // Prevent going back to login
        Toast.makeText(this, "Please use logout button", Toast.LENGTH_SHORT).show();
    }
}