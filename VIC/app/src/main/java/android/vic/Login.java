package android.vic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    public Button login;
    public EditText username, password;
    public ImageView imgView;
    public AnimationDrawable animationDrawable;
    public Context context;
    public String company, department;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        CurrentUser.activityMap.put("Login", this);

        context = Login.this;
        company = "default";
        department = "default";

        // Guobao
        // 先校验是否已经登录
        if (CurrentUser.getInstance(context).isLogan()) {
            messagePart();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            initView();
            addListener();
        }
    }

    // Guobao
    // 双击退出
    private long timestamp = 0;

    @Override
    public void onBackPressed() {
        if (timestamp == 0) {
            Toast.makeText(Login.this, "双击返回键可退出", Toast.LENGTH_SHORT).show();
            timestamp = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - timestamp <= 2000) {
            for (Map.Entry<String, Activity> entry : CurrentUser.activityMap.entrySet()) {
                entry.getValue().finish();
            }
        } else {
            timestamp = System.currentTimeMillis();
        }
    }

    @Override
    public void finish() {
        CurrentUser.activityMap.remove("Login");
        super.finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
        addListener();
        password.setText("");
        imgView.setVisibility(View.INVISIBLE);
    }

    private void initView() {
        if(login             == null) login             = (Button) findViewById(R.id.btn_login);
        if(username          == null) username          = (EditText) findViewById(R.id.et_username);
        if(password          == null) password          = (EditText) findViewById(R.id.et_password);
        if(imgView           == null) imgView           = (ImageView) findViewById(R.id.img_loading);
        if(animationDrawable == null) animationDrawable = (AnimationDrawable) imgView.getDrawable();
    }

    private void addListener() {
        if (!login.hasOnClickListeners()) login.setOnClickListener(new View.OnClickListener() {
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
        // 本地登录功能
        // 切换成服务器登录请注释掉这段代码
        /*------------------------------*/
        CurrentUser.getInstance(context).saveLoginInfo(username, "Cookie", 123, 1, 1, "C1",
                "123123123", "10086", "photoURL", "ADDRESS", "COMPANY", "department", 9123, context);
        Toast.makeText(Login.this, "本地登录", Toast.LENGTH_SHORT).show();
        messagePart();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        if (1 == 1) return;
        /*------------------------------*/
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(4, TimeUnit.SECONDS)
                .writeTimeout(4, TimeUnit.SECONDS)
                .readTimeout(4, TimeUnit.SECONDS)
                .build();
        Toast.makeText(Login.this, "正在登录...", Toast.LENGTH_SHORT).show();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(CurrentUser.IP).addConverterFactory(GsonConverterFactory.create()).client(client).build();
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
                        CurrentUser.getInstance(context).saveLoginInfo(username, response.headers().get("cookie"), response.body().user.id, response.body().user.authority, response.body().user.sex, response.body().user.driverType, response.body().user.identify, response.body().user.phone, response.body().user.photoURL, response.body().user.address, company, department, response.body().user.jobNo, context);
                        getCompany(response.body().user.companyID);
                        getApartment(response.body().user.appartmentID);
                        Toast.makeText(Login.this, "登录成功！", Toast.LENGTH_SHORT).show();
                        // Guobao
                        // 登录成功，调用消息功能
                        messagePart();
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
        // 本地数据
        // 如果要改成服务器，请注释掉本段代码
        /*------------------------------------*/
        company = "company name";
        CurrentUser.getInstance(context).setCompany(company);
        if (1 == 1) return;
        /*------------------------------------*/

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(4, TimeUnit.SECONDS)
                .writeTimeout(4, TimeUnit.SECONDS)
                .readTimeout(4, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(CurrentUser.IP).addConverterFactory(GsonConverterFactory.create()).client(client).build();
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
        // 本地数据
        // 如果要改成服务器，请注释掉本段代码
        /*------------------------------------*/
        department = "department name";
        CurrentUser.getInstance(context).setCompany(department);
        if (1 == 1) return;
        /*------------------------------------*/
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(4, TimeUnit.SECONDS)
                .writeTimeout(4, TimeUnit.SECONDS)
                .readTimeout(4, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(CurrentUser.IP).addConverterFactory(GsonConverterFactory.create()).client(client).build();
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

    // Guobao 合并 朱彦儒 的消息功能
    //DYNAMIC名字根据项目文件可改
    private static final String DYNAMIC = "android.vic.brocastrec";
    private static final int UPDATE_CONTENT = 0;
    private static IntentFilter dynamic_filter = new IntentFilter();

    //用于发送动态广播
    private void sendMessage(Message message) {
        Log.e("MESSAGE", "SEND");
        switch (message.what) {
            case UPDATE_CONTENT:
                Log.e("MESSAGE", "SEND2");
                ArrayList<String> s = (ArrayList<String>) message.obj;
                Bundle bundle = new Bundle();
                //发送标题和内容字符串数组
                bundle.putStringArrayList("message", s);
                Intent intent = new Intent(DYNAMIC);
                intent.putExtras(bundle);
                //用到思全代码
                sendBroadcast(intent);
                break;
            default:
                break;
        }
    }

    public Handler handler = new Handler();
    public Runnable runnable;

    private void messagePart() {
        Log.e("MESSAGE", "message part");
        //获得单例类
        final CurrentUser currentUser = CurrentUser.getInstance(this);
        dynamic_filter.addAction(DYNAMIC);

        //网络是否连接
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() && currentUser.isLogan()) {
            //朱彦儒
            //建立SharedPreferences文件，并定义全局变量
            final SharedPreferences sp = this.getSharedPreferences("Context", MODE_PRIVATE);
            final SharedPreferences.Editor editor = sp.edit();
            final Count count = (Count) getApplicationContext();
            runnable = new Runnable() {
                int i = 1;

                @Override
                public void run() {
                    // 本地数据
                    // 如果要改成服务器，请注释掉本段代码
                    /*------------------------------------*/
                    if (BrocastRec.brocastRec == null) BrocastRec.brocastRec = new BrocastRec();
                    if (currentUser.isLogan()) registerReceiver(BrocastRec.brocastRec, dynamic_filter);
                    ArrayList<String> s_ = new ArrayList<>();
                    s_.add("TITLE" + i);
                    s_.add("CONTENT");
                    i++;
                    if (s_.size() != 0) {
                        int _count = count.getCount();
                        editor.putString("title" + (_count + ""), s_.get(0));
                        editor.putString("content" + (_count + ""), s_.get(1));
                        editor.apply();
                        _count++;
                        count.setCount(_count);
                        Message message = new Message();
                        message.what = UPDATE_CONTENT;
                        message.obj = s_;
                        sendMessage(message);
                    }
                    handler.postDelayed(runnable, 5000);

                    if (1 == 1) return;
                    /*------------------------------------*/


                    // Guobao 修改
                    // 这里我把brocastRec的对象改到了BrocastRec里，做成了一个静态变量
                    // 在这个地方会被注册，在主页面的登出那里可以注销
                    // 第二次修改：在这里进行请求获取数据后，再进行广播和写入文件，然后递归式调用自己
                    if (BrocastRec.brocastRec == null)
                        BrocastRec.brocastRec = new BrocastRec();
                    registerReceiver(BrocastRec.brocastRec, dynamic_filter);
                    ArrayList<String> s = new ArrayList<>();
                    try {
                        // 解析Json得到消息信息
                        // 这里会进行网络请求
                        List<Contexts> _contexts = JsonParse.getListContexts(context);
                        for (int i = 0; i < _contexts.size(); i++) {
                            if (_contexts.get(i).getType() == 0) {
                                s.add(_contexts.get(i).getTitle());
                                s.add(_contexts.get(i).getContent());
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //写入数据
                    if (s.size() != 0) {
                        int _count = count.getCount();
                        editor.putString("title" + (_count + ""), s.get(0));
                        editor.putString("content" + (_count + ""), s.get(1));
                        editor.apply();
                        _count++;
                        count.setCount(_count);
                        Message message = new Message();
                        message.what = UPDATE_CONTENT;
                        message.obj = s;
                        sendMessage(message);
                    }
                    handler.postDelayed(runnable, 5000);
                }
            };
            handler.post(runnable);
            Log.e("MESSAGE", "RUN");
        } else {
            Toast.makeText(Login.this, "当前没有可用网络！", Toast.LENGTH_SHORT).show();
        }
    }
}
