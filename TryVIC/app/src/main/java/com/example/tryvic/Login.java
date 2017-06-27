package com.example.tryvic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.BoolRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by wujy on 2017/6/26.
 * 登录页面逻辑，网络请求
 */

public class Login extends AppCompatActivity {
    public String IP = "http://192.168.1.57:3000/";
    public static final String Preference_Name = "loginState";
    public Button login;
    public EditText account,password;
    public ImageView imgView;
    public AnimationDrawable animationDrawable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        initView();
        addListenner();
    }
    private void initView() {
        login = (Button)findViewById(R.id.btn_login);
        account = (EditText)findViewById(R.id.et_account);
        password = (EditText)findViewById(R.id.et_password);
        imgView = (ImageView)findViewById(R.id.img_loading);
        animationDrawable = (AnimationDrawable)imgView.getDrawable();
    }
    private void addListenner() {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (account.getText().toString().equals("") || password.getText().toString().equals(""))
                    Toast.makeText(Login.this,"账号或密码不能为空！",Toast.LENGTH_SHORT).show();
                else {
                    imgView.setVisibility(View.VISIBLE);
                    animationDrawable.start();
                    login(account.getText().toString(),password.getText().toString());
                }
            }
        });
    }
    public void login(String username, String password) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(4, TimeUnit.SECONDS)
                .writeTimeout(4, TimeUnit.SECONDS)
                .readTimeout(4, TimeUnit.SECONDS)
                .build();
        Toast.makeText(Login.this,"正在登录...",Toast.LENGTH_SHORT).show();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(IP).addConverterFactory(GsonConverterFactory.create()).client(client).build();
        RetrofitInterface service = retrofit.create(RetrofitInterface.class);
        Call<Boolean> call = service.login(username,password);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {

                if (response == null)
                    Log.d("response", "空的");
                else {

                    if (response.body().toString().equals("true")) {

                        SharedPreferences sharedPreferences = getSharedPreferences(Preference_Name,MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLogin",true);
                        editor.commit();
                        animationDrawable.stop();
                        imgView.setVisibility(View.GONE);
                        Toast.makeText(Login.this,"登录成功！",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this,MainActivity.class);
                        startActivity(intent);

                    }
                    else {
                        animationDrawable.stop();
                        imgView.setVisibility(View.GONE);
                        Toast.makeText(Login.this,"登录失败！",Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                animationDrawable.stop();
                imgView.setVisibility(View.GONE);
                Log.d("异常出现",t.getMessage());
                Toast.makeText(Login.this,"出现异常，登录失败！",Toast.LENGTH_SHORT).show();
            }
        });

    }
}
