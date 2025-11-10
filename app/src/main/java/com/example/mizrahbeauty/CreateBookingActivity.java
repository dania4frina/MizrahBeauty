package com.example.mizrahbeauty;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import com.example.mizrahbeauty.models.Staff;
import com.example.mizrahbeauty.adapters.StaffSpinnerAdapter;

public class CreateBookingActivity extends AppCompatActivity {

    private String selectedDate = "";
    private String selectedTime = "";
    private TextView dateTimeDisplay;
    private Spinner staffSpinner;
    private List<Staff> staffList;
    private StaffSpinnerAdapter staffAdapter;
    private Staff selectedStaff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_booking);

        // Initialize views
        EditText serviceIdInput = findViewById(R.id.serviceIdInput);
        staffSpinner = findViewById(R.id.staffSpinner);
        dateTimeDisplay = findViewById(R.id.dateTimeDisplay);
        Button selectDateButton = findViewById(R.id.selectDateButton);
        Button selectTimeButton = findViewById(R.id.selectTimeButton);
        EditText notesInput = findViewById(R.id.notesInput);
        Button submit = findViewById(R.id.submitButton);
        ImageView backButton = findViewById(R.id.backButton);
        
        // Initialize staff list and adapter
        setupStaffSpinner();

        // Set click listeners
        selectDateButton.setOnClickListener(v -> showDatePicker());
        selectTimeButton.setOnClickListener(v -> showTimePicker());
        dateTimeDisplay.setOnClickListener(v -> {
            showDatePicker();
            showTimePicker();
        });
        backButton.setOnClickListener(v -> finish());

        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        int preselectedServiceId = getIntent().getIntExtra("SERVICE_ID", -1);
        String preselectedServiceName = getIntent().getStringExtra("SERVICE_NAME");
        double preselectedServicePrice = getIntent().getDoubleExtra("SERVICE_PRICE", 0.0);
        
        if (preselectedServiceId > 0) {
            serviceIdInput.setText(String.valueOf(preselectedServiceId));
            serviceIdInput.setEnabled(false);
        }

        submit.setOnClickListener(v -> {
            if (userEmail == null || userEmail.isEmpty()) {
                Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String serviceIdStr = serviceIdInput.getText().toString().trim();
            String notes = notesInput.getText().toString().trim();
            
            // Validate service ID
            if (serviceIdStr.isEmpty()) {
                Toast.makeText(this, "Please enter Service ID", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Validate date and time selection
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select date", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedTime.isEmpty()) {
                Toast.makeText(this, "Please select time", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Validate staff selection
            if (selectedStaff == null) {
                Toast.makeText(this, "Please select staff", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Validate staff availability
            if (!selectedStaff.isAvailable()) {
                Toast.makeText(this, "Selected staff is not available. Please choose another staff member.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create combined date and time string
            String appointmentDateTime = selectedDate + " " + selectedTime;
            
            // Validate appointment time
            if (!isValidAppointmentTime(appointmentDateTime)) {
                Toast.makeText(this, "Invalid appointment time. Please ensure the time is at least 30 minutes from now and within business hours (9:00 AM - 7:00 PM)", Toast.LENGTH_LONG).show();
                return;
            }
            
            int serviceId;
            try {
                serviceId = Integer.parseInt(serviceIdStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid Service ID", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Disable submit button to prevent double submission
            submit.setEnabled(false);
            submit.setText("Processing...");
            
            new Thread(() -> {
                // Check for booking conflicts first
                boolean hasConflict = ConnectionClass.hasBookingConflict(appointmentDateTime);
                if (hasConflict) {
                    String conflictInfo = ConnectionClass.getBookingConflictInfo(appointmentDateTime);
                    runOnUiThread(() -> {
                        // Re-enable submit button
                        submit.setEnabled(true);
                        submit.setText("Book Service");
                        
                        String errorMessage = getString(R.string.booking_conflict_title);
                        if (conflictInfo != null) {
                            errorMessage += "\n" + conflictInfo;
                        }
                        errorMessage += "\n\n" + getString(R.string.booking_conflict_choose_different);
                        
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                
                // Proceed with booking creation if no conflict - using staff name as primary identifier
                // Use method that returns booking ID
                int bookingId = ConnectionClass.createBookingWithStaffNameAndGetId(userEmail, serviceId, appointmentDateTime, notes, selectedStaff.getName());
                runOnUiThread(() -> {
                    // Re-enable submit button
                    submit.setEnabled(true);
                    submit.setText("Book Service");
                    
                    if (bookingId > 0) {
                        Toast.makeText(this, "Booking successful! Proceed to payment.", Toast.LENGTH_SHORT).show();
                        
                        // Get service details and navigate to payment method page
                        new Thread(() -> {
                            try {
                                // Use preselected data if available, otherwise get from database
                                String actualServiceName = preselectedServiceName != null ? preselectedServiceName : "Beauty Service";
                                double actualServicePrice = preselectedServicePrice > 0 ? preselectedServicePrice : 50.0;
                                
                                // If no preselected data, get from database
                                if (preselectedServiceName == null || preselectedServicePrice <= 0) {
                                    java.sql.ResultSet serviceRs = ConnectionClass.getServiceById(serviceId);
                                    if (serviceRs != null && serviceRs.next()) {
                                        actualServiceName = serviceRs.getString("service_name");
                                        actualServicePrice = serviceRs.getDouble("price");
                                        serviceRs.close();
                                    }
                                }
                                
                                final String serviceName = actualServiceName;
                                final double servicePrice = actualServicePrice;
                                final int finalBookingId = bookingId;
                                
                                runOnUiThread(() -> {
                                    Intent paymentIntent = new Intent(CreateBookingActivity.this, PaymentMethodActivity.class);
                                    paymentIntent.putExtra("SERVICE_NAME", serviceName);
                                    paymentIntent.putExtra("STAFF_NAME", selectedStaff != null ? selectedStaff.getName() : "Not specified");
                                    paymentIntent.putExtra("APPOINTMENT_DATETIME", appointmentDateTime);
                                    paymentIntent.putExtra("USER_EMAIL", userEmail);
                                    paymentIntent.putExtra("SERVICE_PRICE", servicePrice);
                                    paymentIntent.putExtra("SERVICE_ID", serviceId);
                                    paymentIntent.putExtra("BOOKING_ID", finalBookingId);
                                    startActivity(paymentIntent);
                                    finish();
                                });
                                
                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(() -> {
                                    // Fallback with preselected or default values
                                    String fallbackServiceName = preselectedServiceName != null ? preselectedServiceName : "Beauty Service";
                                    double fallbackServicePrice = preselectedServicePrice > 0 ? preselectedServicePrice : 50.0;
                                    
                                    Intent paymentIntent = new Intent(CreateBookingActivity.this, PaymentMethodActivity.class);
                                    paymentIntent.putExtra("SERVICE_NAME", fallbackServiceName);
                                    paymentIntent.putExtra("STAFF_NAME", selectedStaff != null ? selectedStaff.getName() : "Not specified");
                                    paymentIntent.putExtra("APPOINTMENT_DATETIME", appointmentDateTime);
                                    paymentIntent.putExtra("USER_EMAIL", userEmail);
                                    paymentIntent.putExtra("SERVICE_PRICE", fallbackServicePrice);
                                    paymentIntent.putExtra("SERVICE_ID", serviceId);
                                    startActivity(paymentIntent);
                                    finish();
                                });
                            }
                        }).start();
                    } else {
                        Toast.makeText(this, getString(R.string.booking_failed_message), Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format: YYYY-MM-DD
                    selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                            selectedYear, selectedMonth + 1, selectedDay);
                    updateDateTimeDisplay();
                }, year, month, day);
        
        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    // Validate time is within business hours (9:00 AM to 7:00 PM)
                    if (selectedHour < 9 || selectedHour >= 19) {
                        Toast.makeText(this, "Booking time must be between 9:00 AM and 7:00 PM", Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    // Format: HH:MM
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", 
                            selectedHour, selectedMinute);
                    updateDateTimeDisplay();
                }, hour, minute, true); // true for 24-hour format
        
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        if (!selectedDate.isEmpty() && !selectedTime.isEmpty()) {
            String appointmentDateTime = selectedDate + " " + selectedTime;
            dateTimeDisplay.setText(appointmentDateTime);
            
            // Check for conflicts in real-time
            checkForConflicts(appointmentDateTime);
        } else if (!selectedDate.isEmpty()) {
            dateTimeDisplay.setText(selectedDate + " (Select time)");
        } else if (!selectedTime.isEmpty()) {
            dateTimeDisplay.setText("(Select date) " + selectedTime);
        } else {
            dateTimeDisplay.setText("Tap to select date and time");
        }
    }
    
    private void checkForConflicts(String appointmentDateTime) {
        // Check for conflicts in background thread
        new Thread(() -> {
            boolean hasConflict = ConnectionClass.hasBookingConflict(appointmentDateTime);
            runOnUiThread(() -> {
                if (hasConflict) {
                    // Show visual indication of conflict
                    dateTimeDisplay.setTextColor(0xFFFF0000); // Red color
                    dateTimeDisplay.setText(selectedDate + " " + selectedTime + " (CONFLICT!)");
                    
                    // Show brief toast about conflict
                    Toast.makeText(this, getString(R.string.booking_conflict_message), Toast.LENGTH_SHORT).show();
                } else {
                    // Reset to normal color
                    dateTimeDisplay.setTextColor(0xFF000000); // Black color
                    dateTimeDisplay.setText(selectedDate + " " + selectedTime);
                }
            });
        }).start();
    }
    
    private boolean isValidAppointmentTime(String appointmentDateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date appointmentDate = sdf.parse(appointmentDateTime);
            Date currentDate = new Date();
            
            // Check if appointment is at least 30 minutes from now
            long timeDifference = appointmentDate.getTime() - currentDate.getTime();
            long thirtyMinutesInMillis = 30 * 60 * 1000; // 30 minutes in milliseconds
            
            if (timeDifference < thirtyMinutesInMillis) {
                return false;
            }
            
            // Check if appointment is within business hours (9:00 AM to 7:00 PM)
            Calendar appointmentCalendar = Calendar.getInstance();
            appointmentCalendar.setTime(appointmentDate);
            int appointmentHour = appointmentCalendar.get(Calendar.HOUR_OF_DAY);
            
            // Business hours: 9:00 AM (9) to 7:00 PM (19)
            if (appointmentHour < 9 || appointmentHour >= 19) {
                return false;
            }
            
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void setupStaffSpinner() {
        staffList = new ArrayList<>();
        
        // Add default "Select Staff" option
        Staff defaultStaff = new Staff();
        defaultStaff.setName("Select Staff");
        defaultStaff.setPosition("");
        defaultStaff.setAvailable(true);
        staffList.add(defaultStaff);
        
        // Load staff from database in background thread
        new Thread(() -> {
            try {
                List<Staff> availableStaff = ConnectionClass.getAvailableStaffList();
                
                runOnUiThread(() -> {
                    if (availableStaff != null && !availableStaff.isEmpty()) {
                        staffList.addAll(availableStaff);
                    }
                    
                    // Setup adapter
                    staffAdapter = new StaffSpinnerAdapter(this, staffList);
                    staffSpinner.setAdapter(staffAdapter);
                    
                    // Set selection listener
                    staffSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                            if (position > 0) { // Skip default "Select Staff" option
                                Staff staff = staffList.get(position);
                                if (staff.isAvailable()) {
                                    selectedStaff = staff;
                                } else {
                                    // Staff not available, reset selection and show message
                                    selectedStaff = null;
                                    staffSpinner.setSelection(0); // Reset to "Select Staff"
                                    Toast.makeText(CreateBookingActivity.this, 
                                        "Staff " + staff.getName() + " is not available for booking", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                selectedStaff = null;
                            }
                        }
                        
                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {
                            selectedStaff = null;
                        }
                    });
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to load staff list", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}


