package com.example.mizrahbeauty;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        EditText serviceIdInput = new EditText(this);
        serviceIdInput.setHint("Service ID");
        serviceIdInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        root.addView(serviceIdInput);

        EditText ratingInput = new EditText(this);
        ratingInput.setHint("Rating (1-5)");
        ratingInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        root.addView(ratingInput);

        EditText commentInput = new EditText(this);
        commentInput.setHint("Ulasan");
        root.addView(commentInput);

        Button submit = new Button(this);
        submit.setText("Hantar Review");
        root.addView(submit);

        setContentView(root);

        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        int prefilledServiceId = getIntent().getIntExtra("SERVICE_ID", -1);
        if (prefilledServiceId > 0) {
            serviceIdInput.setText(String.valueOf(prefilledServiceId));
            serviceIdInput.setEnabled(false);
        }

        submit.setOnClickListener(v -> {
            if (userEmail == null || userEmail.isEmpty()) {
                Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
                return;
            }
            String serviceIdStr = serviceIdInput.getText().toString().trim();
            String ratingStr = ratingInput.getText().toString().trim();
            String comment = commentInput.getText().toString().trim();
            if (serviceIdStr.isEmpty() || ratingStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            int serviceId = Integer.parseInt(serviceIdStr);
            int rating = Integer.parseInt(ratingStr);
            if (rating < 1 || rating > 5) {
                Toast.makeText(this, "Rating 1-5", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                boolean ok = ConnectionClass.addReview(userEmail, serviceId, rating, comment);
                runOnUiThread(() -> {
                    if (ok) {
                        Toast.makeText(this, "Review submitted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });
    }
}


