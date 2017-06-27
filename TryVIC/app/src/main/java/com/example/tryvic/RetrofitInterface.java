package com.example.tryvic;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by wujy on 2017/6/26.
 */

public interface RetrofitInterface {

    @FormUrlEncoded
    @POST("login")
    Call<Boolean> login(@Field("username") String username, @Field("password") String password);
}
