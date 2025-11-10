package com.example.mizrahbeauty.payment;

import androidx.annotation.NonNull;

import com.example.mizrahbeauty.payment.requests.CreateBillRequest;
import com.example.mizrahbeauty.payment.responses.CreateBillResponse;
import com.example.mizrahbeauty.payment.responses.PaymentStatusResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository wrapper for ToyyibPay operations.
 */
public class ToyyibPayRepository {

    private final ToyyibPayApi api;

    public interface CreateBillCallback {
        void onSuccess(CreateBillResponse response);
        void onError(Throwable t);
    }

    public interface StatusCallback {
        void onSuccess(PaymentStatusResponse response);
        void onError(Throwable t);
    }

    public ToyyibPayRepository() {
        this.api = ToyyibPayClient.getApi();
    }

    public void createBill(CreateBillRequest request, CreateBillCallback callback) {
        api.createBill(request).enqueue(new Callback<CreateBillResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateBillResponse> call, @NonNull Response<CreateBillResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new IllegalStateException("Failed to create bill: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreateBillResponse> call, @NonNull Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void getPaymentStatus(String billCode, StatusCallback callback) {
        api.getPaymentStatus(billCode).enqueue(new Callback<PaymentStatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaymentStatusResponse> call, @NonNull Response<PaymentStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new IllegalStateException("Failed to retrieve status: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaymentStatusResponse> call, @NonNull Throwable t) {
                callback.onError(t);
            }
        });
    }
}

