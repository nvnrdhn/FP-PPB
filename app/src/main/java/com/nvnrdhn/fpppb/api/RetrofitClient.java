package com.nvnrdhn.fpppb.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    private static OkHttpClient httpClient = null;

    private static String auth;

    public static Retrofit getRetrofitClient(String baseUrl, String token){

        auth = token;

        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(getHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static OkHttpClient getHttpClient() {

        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(new AuthInterceptor())
                    .build();
        }

        return httpClient;
    }

    static final class AuthInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request req = chain.request().newBuilder()
//                    .header("Connection", "close")
//                    .header("Accept", "application/json")
                    .build();
            return chain.proceed(req);
        }
    }
}
