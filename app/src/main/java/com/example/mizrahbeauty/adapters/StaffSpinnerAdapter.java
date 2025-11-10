package com.example.mizrahbeauty.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.mizrahbeauty.R;
import com.example.mizrahbeauty.models.Staff;

import java.util.List;

public class StaffSpinnerAdapter extends BaseAdapter {
    private Context context;
    private List<Staff> staffList;
    private LayoutInflater inflater;

    public StaffSpinnerAdapter(Context context, List<Staff> staffList) {
        this.context = context;
        this.staffList = staffList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return staffList.size();
    }

    @Override
    public Object getItem(int position) {
        return staffList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_staff_spinner_selected, parent, false);
        }

        Staff staff = staffList.get(position);
        TextView nameText = view.findViewById(R.id.staffNameSelectedText);
        // Only show staff name, no position
        String displayName = staff.getName();
        nameText.setText(displayName);

        boolean isPlaceholder = (position == 0) && (staff.getUserEmail() == null || staff.getUserEmail().isEmpty());
        int color = ContextCompat.getColor(context, isPlaceholder ? R.color.text_secondary : R.color.text_primary);
        nameText.setTextColor(color);
        view.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_staff_spinner, parent, false);
        }

        Staff staff = staffList.get(position);

        TextView staffNameText = view.findViewById(R.id.staffNameText);
        TextView staffPositionText = view.findViewById(R.id.staffPositionText);
        TextView staffStatusText = view.findViewById(R.id.staffStatusText);

        staffNameText.setText(staff.getName());

        // Hide position text - only show staff name
        staffPositionText.setVisibility(View.GONE);

        boolean isPlaceholder = (position == 0) && (staff.getUserEmail() == null || staff.getUserEmail().isEmpty());

        if (isPlaceholder) {
            staffStatusText.setText("");
            staffStatusText.setVisibility(View.GONE);
            view.setEnabled(true);
        } else {
            staffStatusText.setVisibility(View.VISIBLE);
            staffStatusText.setText(staff.getAvailabilityStatus());

            int statusColor = ContextCompat.getColor(context,
                    staff.isAvailable() ? R.color.success_green : R.color.warning_orange);
            staffStatusText.setTextColor(statusColor);
            
            // Disable unavailable staff
            boolean isAvailable = staff.isAvailable();
            view.setEnabled(isAvailable);
            view.setAlpha(isAvailable ? 1.0f : 0.5f);
            
            // Update text colors for disabled items
            if (!isAvailable) {
                staffNameText.setTextColor(ContextCompat.getColor(context, R.color.text_disabled));
                staffPositionText.setTextColor(ContextCompat.getColor(context, R.color.text_disabled));
            } else {
                staffNameText.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                staffPositionText.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }
        }

        return view;
    }
    
    @Override
    public boolean isEnabled(int position) {
        // Disable selection for unavailable staff (except placeholder)
        if (position == 0) return true; // Always allow placeholder selection
        Staff staff = staffList.get(position);
        return staff.isAvailable();
    }
}
