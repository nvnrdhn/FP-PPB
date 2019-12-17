package com.nvnrdhn.fpppb.api;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface BaseApiService {
    @Multipart
    @POST("image")
    Call<FaceData> detect(@Part MultipartBody.Part photo);
}
