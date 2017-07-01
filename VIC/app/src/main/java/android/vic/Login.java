package android.vic;

/**
 * Created by wujy on 2017/7/1.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.vic.RetrofitEntity.DepartmentRetro;
import android.vic.RetrofitEntity.LoginRetro;
import android.vic.RetrofitEntity.UnitRetro;
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
 * 登录页面
 */

public class Login extends AppCompatActivity {
    public String IP = "http://192.168.1.57:3000/";
    public static final String Preference_Name = "loginState";
    public Button login;
    public EditText username, password;
    public ImageView imgView;
    public AnimationDrawable animationDrawable;
    public Context context;
    public String company,department;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        context = Login.this;
        company = "default";
        department = "default";
        initView();
        addListenner();
    }

    private void initView() {
        login = (Button) findViewById(R.id.btn_login);
        username = (EditText) findViewById(R.id.et_username);
        password = (EditText) findViewById(R.id.et_password);
        imgView = (ImageView) findViewById(R.id.img_loading);
        animationDrawable = (AnimationDrawable) imgView.getDrawable();
    }

    private void addListenner() {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (username.getText().toString().equals("") || password.getText().toString().equals(""))
                    Toast.makeText(Login.this, "账号或密码不能为空！", Toast.LENGTH_SHORT).show();
                else {
                    imgView.setVisibility(View.VISIBLE);
                    animationDrawable.start();
                    login(username.getText().toString(), password.getText().toString());
                }
            }
        });
    }

    public void login(final String username, String password) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(4, TimeUnit.SECONDS)
                .writeTimeout(4, TimeUnit.SECONDS)
                .readTimeout(4, TimeUnit.SECONDS)
                .build();
        Toast.makeText(Login.this, "正在登录...", Toast.LENGTH_SHORT).show();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(IP).addConverterFactory(GsonConverterFactory.create()).client(client).build();
        RetrofitInterface service = retrofit.create(RetrofitInterface.class);
        Call<LoginRetro> call = service.login(username, password);
        call.enqueue(new Callback<LoginRetro>() {
            @Override
            public void onResponse(Call<LoginRetro> call, Response<LoginRetro> response) {

                if (response == null)
                    Log.d("response", "空的");
                else {

                    if (response.body().status == 1) {

                        animationDrawable.stop();
                        imgView.setVisibility(View.INVISIBLE);
                        CurrentUser.getInstance(context).saveLoginInfo(username,response.headers().get("sessionID"),response.body().user.id,response.body().user.authority,response.body().user.sex,response.body().user.driverType,response.body().user.identify,response.body().user.phone,response.body().user.photoURL,response.body().user.address,company,department,response.body().user.jobNo,context);
                        getCompany(response.body().user.companyID);
                        getApartment(response.body().user.appartmentID);
                        Toast.makeText(Login.this, "登录成功！", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);

                    } else {
                        animationDrawable.stop();
                        imgView.setVisibility(View.INVISIBLE);
                        Toast.makeText(Login.this, "登录失败！", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onFailure(Call<LoginRetro> call, Throwable t) {
                animationDrawable.stop();
                imgView.setVisibility(View.INVISIBLE);
                Log.d("异常出现", t.getMessage());
                Toast.makeText(Login.this, "出现异常，登录失败！", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void getCompany(int companyID) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(4, TimeUnit.SECONDS)
                .writeTimeout(4, TimeUnit.SECONDS)
                .readTimeout(4, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(IP).addConverterFactory(GsonConverterFactory.create()).client(client).build();
        RetrofitInterface service = retrofit.create(RetrofitInterface.class);
        Call<UnitRetro> call = service.getUnit(companyID);
        call.enqueue(new Callback<UnitRetro>() {
            @Override
            public void onResponse(Call<UnitRetro> call, Response<UnitRetro> response) {

                if (response == null)
                    Log.d("response", "空的");
                else {

                    if (response.body().status == 1) {
                       company = response.body().unit.name;
                       //
                        CurrentUser.getInstance(context).setCompany(company);

                    } else {


                        Toast.makeText(Login.this, "公司获取失败！", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onFailure(Call<UnitRetro> call, Throwable t) {
                animationDrawable.stop();
                imgView.setVisibility(View.GONE);
                Log.d("异常出现", t.getMessage());
                Toast.makeText(Login.this, "出现异常，公司获取失败！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getApartment(int apartmentID) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(4, TimeUnit.SECONDS)
                .writeTimeout(4, TimeUnit.SECONDS)
                .readTimeout(4, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(IP).addConverterFactory(GsonConverterFactory.create()).client(client).build();
        RetrofitInterface service = retrofit.create(RetrofitInterface.class);
        Call<DepartmentRetro> call = service.getDepartment(apartmentID);
        call.enqueue(new Callback<DepartmentRetro>() {
            @Override
            public void onResponse(Call<DepartmentRetro> call, Response<DepartmentRetro> response) {

                if (response == null)
                    Log.d("response", "空的");
                else {

                    if (response.body().status == 1) {
                        department = response.body().department.name;
                        //
                        CurrentUser.getInstance(context).setApartment(department);

                    } else {


                        Toast.makeText(Login.this, "部门获取失败！", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onFailure(Call<DepartmentRetro> call, Throwable t) {

                Log.d("异常出现", t.getMessage());
                Toast.makeText(Login.this, "出现异常，获取部门失败！", Toast.LENGTH_SHORT).show();
            }
        });
    }
}