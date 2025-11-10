package com.example.mizrahbeauty;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserBookingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView listView = new ListView(this);
        setContentView(listView);

        String userEmail = getIntent().getStringExtra("USER_EMAIL");

        new Thread(() -> {
            List<String> items = new ArrayList<>();
            List<Integer> bookingIds = new ArrayList<>();
            List<Integer> serviceIds = new ArrayList<>();
            List<String> serviceNames = new ArrayList<>();
            List<String> appointmentTimes = new ArrayList<>();
            try {
                ResultSet rs = ConnectionClass.getUserBookings(userEmail);
                if (rs != null) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        bookingIds.add(id);
                        serviceIds.add(rs.getInt("service_id"));
                        String serviceName = rs.getString("service_name");
                        String appt = rs.getString("appointment_time");
                        String status = rs.getString("status");
                        serviceNames.add(serviceName);
                        appointmentTimes.add(appt);
                        items.add(formatRow(id, serviceName, appt, status));
                    }
                    try { rs.close(); } catch (Exception ignored) {}
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to load bookings", Toast.LENGTH_SHORT).show());
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener((parent, view, position, id) -> {
                    Integer bookingId = bookingIds.get(position);
                    cancelBooking(bookingId, userEmail, adapter, items, position, bookingIds, serviceNames, appointmentTimes);
                });
                listView.setOnItemLongClickListener((parent, view, position, id) -> {
                    Integer serviceId = serviceIds.get(position);
                    startAddReview(userEmail, serviceId);
                    return true;
                });
            });
        }).start();
    }

    private String formatRow(int id, String serviceName, String appointmentTime, String status) {
        return "#" + id + " - " + serviceName + " | " + appointmentTime + " | " + status;
    }

    private void cancelBooking(int bookingId, String userEmail, ArrayAdapter<String> adapter, List<String> items, int index,
                               List<Integer> bookingIds, List<String> serviceNames, List<String> appointmentTimes) {
        new Thread(() -> {
            boolean ok = ConnectionClass.cancelBooking(bookingId, userEmail);
            runOnUiThread(() -> {
                if (ok) {
                    Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show();
                    // Keep item in list, only change status to CANCELLED
                    int id = bookingIds.get(index);
                    String name = serviceNames.get(index);
                    String appt = appointmentTimes.get(index);
                    items.set(index, formatRow(id, name, appt, "CANCELLED"));
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Failed to cancel booking", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void startAddReview(String userEmail, int serviceId) {
        android.content.Intent i = new android.content.Intent(this, AddReviewActivity.class);
        i.putExtra("USER_EMAIL", userEmail);
        i.putExtra("SERVICE_ID", serviceId);
        startActivity(i);
    }
}


