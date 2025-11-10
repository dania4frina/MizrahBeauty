package com.example.mizrahbeauty.payment;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Configures Retrofit client for ToyyibPay backend interaction.
 */
public final class ToyyibPayClient {

    private static final String BASE_URL = "https://mizrahbeauty-toyyibpay.onrender.com/";

    private static Retrofit retrofitInstance;

    private ToyyibPayClient() {
        // no-op
    }

    public static ToyyibPayApi getApi() {
        if (retrofitInstance == null) {
            synchronized (ToyyibPayClient.class) {
                if (retrofitInstance == null) {
                    if (TextUtils.isEmpty(BASE_URL)) {
                        throw new IllegalStateException("ToyyibPayClient BASE_URL must not be empty.");
                    }

                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.level(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(logging)
                            .build();

                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    retrofitInstance = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                }
            }
        }
        return retrofitInstance.create(ToyyibPayApi.class);
    }
}

