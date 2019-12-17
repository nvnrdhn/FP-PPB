package com.nvnrdhn.fpppb.api;

public class UtilsApi {
    public static final String BASE_URL_API = Settings.address;

    public static  BaseApiService apiService;

    public static void getAPIService(String token){
        apiService = RetrofitClient.getRetrofitClient(BASE_URL_API, token).create(BaseApiService.class);
    }
}
