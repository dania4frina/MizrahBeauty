package com.example.mizrahbeauty.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mizrahbeauty.R;
import com.example.mizrahbeauty.models.Feedback;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {
    private List<Feedback> feedbackList;
    private Context context;

    public FeedbackAdapter(List<Feedback> feedbackList) {
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);
        holder.bind(feedback);
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public class FeedbackViewHolder extends RecyclerView.ViewHolder {
        private TextView feedbackTypeText, userNameText, feedbackText, createdAtText, responseText, respondedAtText;
        private RatingBar ratingBar;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            feedbackTypeText = itemView.findViewById(R.id.feedbackTypeText);
            userNameText = itemView.findViewById(R.id.userNameText);
            feedbackText = itemView.findViewById(R.id.feedbackText);
            createdAtText = itemView.findViewById(R.id.createdAtText);
            responseText = itemView.findViewById(R.id.responseText);
            respondedAtText = itemView.findViewById(R.id.respondedAtText);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }

        public void bind(Feedback feedback) {
            feedbackTypeText.setText(feedback.getFeedbackType());
            String displayName = feedback.getUserName();
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = feedback.getUserEmail() != null ? feedback.getUserEmail() : "Pengguna";
            }
            userNameText.setText(displayName);
            feedbackText.setText(feedback.getFeedbackText());
            createdAtText.setText(feedback.getFormattedCreatedAt());
            ratingBar.setRating(feedback.getRating());

            // Show response if available
            if (feedback.getResponse() != null && !feedback.getResponse().isEmpty()) {
                responseText.setVisibility(View.VISIBLE);
                responseText.setText("Respons: " + feedback.getResponse());
                respondedAtText.setVisibility(View.VISIBLE);
                respondedAtText.setText("Direspons pada: " + feedback.getFormattedRespondedAt());
            } else {
                responseText.setVisibility(View.GONE);
                respondedAtText.setVisibility(View.GONE);
            }
        }
    }
}
