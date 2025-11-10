package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
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

public class FeedbackActivity extends AppCompatActivity {
    private String userEmail, userName;
    private TextView titleText, feedbackCountText;
    private ImageView backButton;
    private Button submitFeedbackButton, viewFeedbackButton;
    private LinearLayout submitFeedbackLayout, viewFeedbackLayout;
    private RecyclerView feedbackRecyclerView;
    private FeedbackAdapter feedbackAdapter;
    private List<Feedback> feedbackList;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Get user info from intent
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        userName = getIntent().getStringExtra("USER_NAME");

        initializeViews();
        setupClickListeners();
        setupRecyclerView();
        loadFeedback();
    }

    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        feedbackCountText = findViewById(R.id.feedbackCountText);
        backButton = findViewById(R.id.backButton);
        submitFeedbackButton = findViewById(R.id.submitFeedbackButton);
        viewFeedbackButton = findViewById(R.id.viewFeedbackButton);
        submitFeedbackLayout = findViewById(R.id.submitFeedbackLayout);
        viewFeedbackLayout = findViewById(R.id.viewFeedbackLayout);
        feedbackRecyclerView = findViewById(R.id.feedbackRecyclerView);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Set initial state
        showSubmitFeedback();
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        submitFeedbackButton.setOnClickListener(v -> {
            showSubmitFeedback();
        });

        viewFeedbackButton.setOnClickListener(v -> {
            showViewFeedback();
        });

        // Submit feedback form
        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> submitFeedback());
    }

    private void setupRecyclerView() {
        feedbackList = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter(feedbackList);
        feedbackRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedbackRecyclerView.setAdapter(feedbackAdapter);
    }

    private void showSubmitFeedback() {
        submitFeedbackLayout.setVisibility(View.VISIBLE);
        viewFeedbackLayout.setVisibility(View.GONE);
        submitFeedbackButton.setBackgroundResource(R.drawable.button_primary);
        viewFeedbackButton.setBackgroundResource(R.drawable.button_secondary);
    }

    private void showViewFeedback() {
        submitFeedbackLayout.setVisibility(View.GONE);
        viewFeedbackLayout.setVisibility(View.VISIBLE);
        submitFeedbackButton.setBackgroundResource(R.drawable.button_secondary);
        viewFeedbackButton.setBackgroundResource(R.drawable.button_primary);
        loadFeedback();
    }

    private void submitFeedback() {
        EditText feedbackText = findViewById(R.id.feedbackText);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        Spinner feedbackTypeSpinner = findViewById(R.id.feedbackTypeSpinner);

        String feedback = feedbackText.getText().toString().trim();
        int rating = (int) ratingBar.getRating();
        String feedbackType = feedbackTypeSpinner.getSelectedItem().toString();

        if (feedback.isEmpty()) {
            Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rating == 0) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            System.out.println("=== FEEDBACK ACTIVITY DEBUG ===");
            System.out.println("User Email: " + userEmail);
            System.out.println("User Name: " + userName);
            System.out.println("Feedback: " + feedback);
            System.out.println("Rating: " + rating);
            System.out.println("Feedback Type: " + feedbackType);
            
            boolean success = ConnectionClass.submitFeedback(userEmail, userName, feedback, rating, feedbackType);
            System.out.println("Submit result: " + success);
            
            mainHandler.post(() -> {
                if (success) {
                    Toast.makeText(this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                    feedbackText.setText("");
                    ratingBar.setRating(0);
                    feedbackTypeSpinner.setSelection(0);
                } else {
                    Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadFeedback() {
        executorService.execute(() -> {
            try {
                List<Feedback> allFeedback = ConnectionClass.getAllFeedbackList();

                mainHandler.post(() -> {
                    feedbackList.clear();
                    if (allFeedback != null) {
                        feedbackList.addAll(allFeedback);
                    }
                    feedbackAdapter.notifyDataSetChanged();
                    updateFeedbackCount();
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> Toast.makeText(this, "Failed to load feedback", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateFeedbackCount() {
        int count = feedbackList.size();
        feedbackCountText.setText(count + " feedback");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
