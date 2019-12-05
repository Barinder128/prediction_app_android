package com.example.priceforecast;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Api {
    //HTTP annotation providing request method and relative URL
    @POST("/")
    Call<ResponseBody> postUser(@Body RequestBody requestBody);
}
