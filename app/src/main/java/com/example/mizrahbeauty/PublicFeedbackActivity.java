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

import com.example.mizrahbeauty.adapters.PublicFeedbackAdapter;
import com.example.mizrahbeauty.models.Feedback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PublicFeedbackActivity extends AppCompatActivity {
    private TextView titleText, totalFeedbackText, averageRatingText;
    private ImageView backButton;
    private RecyclerView publicFeedbackRecyclerView;
    private PublicFeedbackAdapter feedbackAdapter;
    private List<Feedback> allFeedbackList;
    private List<Feedback> filteredFeedbackList;
    private ExecutorService executorService;
    private Handler mainHandler;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_feedback);

        initializeViews();
        setupClickListeners();
        setupRecyclerView();
        loadAllFeedback();
    }

    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        totalFeedbackText = findViewById(R.id.totalFeedbackText);
        averageRatingText = findViewById(R.id.averageRatingText);
        backButton = findViewById(R.id.backButton);
        publicFeedbackRecyclerView = findViewById(R.id.publicFeedbackRecyclerView);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        allFeedbackList = new ArrayList<>();
        filteredFeedbackList = new ArrayList<>();
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        feedbackAdapter = new PublicFeedbackAdapter(filteredFeedbackList);
        publicFeedbackRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        publicFeedbackRecyclerView.setAdapter(feedbackAdapter);
    }

    private void loadAllFeedback() {
        executorService.execute(() -> {
            try {
                List<Feedback> feedbackList = ConnectionClass.getAllFeedbackList();
                
                mainHandler.post(() -> {
                    allFeedbackList.clear();
                    filteredFeedbackList.clear();
                    if (feedbackList != null) {
                        // Show ALL feedback to users
                        allFeedbackList.addAll(feedbackList);
                        filteredFeedbackList.addAll(feedbackList);
                    }
                    
                    updateStats();
                    feedbackAdapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    Toast.makeText(this, "Failed to load feedback", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    private void updateStats() {
        int totalCount = allFeedbackList.size();
        totalFeedbackText.setText(String.valueOf(totalCount));
        
        if (totalCount > 0) {
            double totalRating = 0;
            for (Feedback feedback : allFeedbackList) {
                totalRating += feedback.getRating();
            }
            double averageRating = totalRating / totalCount;
            averageRatingText.setText(String.format("%.1f", averageRating));
        } else {
            averageRatingText.setText("0.0");
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
