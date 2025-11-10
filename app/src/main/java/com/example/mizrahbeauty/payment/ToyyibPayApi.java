package com.example.mizrahbeauty.payment;

import com.example.mizrahbeauty.payment.requests.CreateBillRequest;
import com.example.mizrahbeauty.payment.responses.CreateBillResponse;
import com.example.mizrahbeauty.payment.responses.PaymentStatusResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit interface for communicating with your backend ToyyibPay proxy.
 * The backend should wrap the actual ToyyibPay APIs so that the Android app
 * never handles secret keys directly.
 */
public interface ToyyibPayApi {

    @POST("toyyibpay/payment/bill")
    Call<CreateBillResponse> createBill(@Body CreateBillRequest request);

    @GET("toyyibpay/payment/status")
    Call<PaymentStatusResponse> getPaymentStatus(@Query("billCode") String billCode);
}

