package com.example.mizrahbeauty;

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

import com.example.mizrahbeauty.adapters.FeedbackAdapter;
import com.example.mizrahbeauty.models.Feedback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StaffFeedbackActivity extends AppCompatActivity {
    private String staffEmail, staffName;
    private TextView titleText, feedbackCountText;
    private ImageView backButton;
    private RecyclerView feedbackRecyclerView;
    private FeedbackAdapter feedbackAdapter;
    private List<Feedback> feedbackList;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_feedback);

        // Get staff info from intent
        staffEmail = getIntent().getStringExtra("USER_EMAIL");
        staffName = getIntent().getStringExtra("USER_NAME");

        initializeViews();
        setupClickListeners();
        setupRecyclerView();
        loadAllFeedback();
    }

    private void initializeViews() {
        try {
            System.out.println("=== INITIALIZING STAFF FEEDBACK VIEWS ===");
            titleText = findViewById(R.id.titleText);
            feedbackCountText = findViewById(R.id.feedbackCountText);
            backButton = findViewById(R.id.backButton);
            feedbackRecyclerView = findViewById(R.id.feedbackRecyclerView);

            executorService = Executors.newSingleThreadExecutor();
            mainHandler = new Handler(Looper.getMainLooper());

            // Set title
            titleText.setText(getString(R.string.customer_feedback));
            System.out.println("Staff feedback views initialized successfully");
        } catch (Exception e) {
            System.out.println("Error initializing staff feedback views: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

    }

    private void setupRecyclerView() {
        try {
            System.out.println("=== SETTING UP FEEDBACK RECYCLER VIEW ===");
            feedbackList = new ArrayList<>();
            feedbackAdapter = new FeedbackAdapter(feedbackList);
            feedbackRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            feedbackRecyclerView.setAdapter(feedbackAdapter);
            System.out.println("Feedback RecyclerView setup completed successfully");
        } catch (Exception e) {
            System.out.println("Error setting up feedback RecyclerView: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error setting up RecyclerView: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadAllFeedback() {
        executorService.execute(() -> {
            try {
                System.out.println("=== LOADING ALL FEEDBACK ===");
                List<Feedback> allFeedback = ConnectionClass.getAllFeedbackList();
                
                mainHandler.post(() -> {
                    try {
                        feedbackList.clear();
                        if (allFeedback != null && !allFeedback.isEmpty()) {
                            feedbackList.addAll(allFeedback);
                            System.out.println("Loaded " + allFeedback.size() + " feedback items");
                        } else {
                            System.out.println("No feedback found");
                        }
                        
                        feedbackAdapter.notifyDataSetChanged();
                        updateFeedbackCount();
                        System.out.println("UI updated successfully");
                    } catch (Exception e) {
                        System.out.println("Error updating UI: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(this, "Error updating UI", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                System.out.println("Error loading feedback: " + e.getMessage());
                e.printStackTrace();
                mainHandler.post(() -> {
                    Toast.makeText(this, "Failed to load feedback: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateFeedbackCount() {
        int count = feedbackList.size();
        feedbackCountText.setText(count + " " + getString(R.string.feedback).toLowerCase());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from other activities
        loadAllFeedback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
