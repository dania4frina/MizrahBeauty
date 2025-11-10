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
import com.example.mizrahbeauty.models.Review;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    
    private List<Review> reviewList;
    private Context context;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        
        holder.customerEmailText.setText("Customer: " + review.getUserEmail());
        holder.serviceNameText.setText("Service: " + review.getServiceName());
        holder.ratingBar.setRating(review.getRating());
        holder.ratingText.setText(review.getRating() + "/5");
        holder.createdAtText.setText("Date: " + formatDateTime(review.getCreatedAt()));
        
        if (review.getComment() != null && !review.getComment().isEmpty()) {
            holder.commentText.setVisibility(View.VISIBLE);
            holder.commentText.setText("\"" + review.getComment() + "\"");
        } else {
            holder.commentText.setVisibility(View.GONE);
        }
    }

    private String formatDateTime(String dateTimeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateTimeString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateTimeString; // Return original string if parsing fails
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public void updateReviews(List<Review> newReviews) {
        this.reviewList = newReviews;
        notifyDataSetChanged();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView customerEmailText, serviceNameText, ratingText, commentText, createdAtText;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            customerEmailText = itemView.findViewById(R.id.customerEmailText);
            serviceNameText = itemView.findViewById(R.id.serviceNameText);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            ratingText = itemView.findViewById(R.id.ratingText);
            commentText = itemView.findViewById(R.id.commentText);
            createdAtText = itemView.findViewById(R.id.createdAtText);
        }
    }
}
