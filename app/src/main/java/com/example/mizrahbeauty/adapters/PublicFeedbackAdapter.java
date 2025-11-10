package com.example.mizrahbeauty.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mizrahbeauty.R;
import com.example.mizrahbeauty.models.Feedback;

import java.util.List;

public class PublicFeedbackAdapter extends RecyclerView.Adapter<PublicFeedbackAdapter.PublicFeedbackViewHolder> {
    private List<Feedback> feedbackList;
    private Context context;

    public PublicFeedbackAdapter(List<Feedback> feedbackList) {
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public PublicFeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_public_feedback, parent, false);
        return new PublicFeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicFeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);
        holder.bind(feedback);
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public class PublicFeedbackViewHolder extends RecyclerView.ViewHolder {
        private TextView userAvatarText, userNameText, createdAtText, feedbackTypeText;
        private TextView feedbackText, responseText, respondedAtText, ratingText;
        private RatingBar ratingBar;
        private LinearLayout responseLayout;

        public PublicFeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatarText = itemView.findViewById(R.id.userAvatarText);
            userNameText = itemView.findViewById(R.id.userNameText);
            createdAtText = itemView.findViewById(R.id.createdAtText);
            feedbackTypeText = itemView.findViewById(R.id.feedbackTypeText);
            feedbackText = itemView.findViewById(R.id.feedbackText);
            responseText = itemView.findViewById(R.id.responseText);
            respondedAtText = itemView.findViewById(R.id.respondedAtText);
            ratingText = itemView.findViewById(R.id.ratingText);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            responseLayout = itemView.findViewById(R.id.responseLayout);
        }

        public void bind(Feedback feedback) {
            // Show full user name
            String displayName = feedback.getUserName();
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = "Pengguna";
            }
            userNameText.setText(displayName);
            
            // Set avatar with first letter of name
            String avatarLetter = displayName.substring(0, 1).toUpperCase();
            userAvatarText.setText(avatarLetter);
            
            feedbackTypeText.setText(feedback.getFeedbackType());
            feedbackText.setText(feedback.getFeedbackText());
            createdAtText.setText(feedback.getFormattedCreatedAt());
            ratingBar.setRating(feedback.getRating());
            ratingText.setText(String.valueOf(feedback.getRating()));

            // Show response if available
            if (feedback.getResponse() != null && !feedback.getResponse().isEmpty()) {
                responseLayout.setVisibility(View.VISIBLE);
                responseText.setText(feedback.getResponse());
                respondedAtText.setText("Direspons pada: " + feedback.getFormattedRespondedAt());
            } else {
                responseLayout.setVisibility(View.GONE);
            }
        }
    }
}
