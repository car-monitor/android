package android.vic;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
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

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
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
            Toast.makeText(Login.this, "再次点击返回键可退出", Toast.LENGTH_SHORT).show();
            timestamp = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - timestamp <= Toast.LENGTH_SHORT) {
            System.exit(0);
        } else {
            timestamp = System.currentTimeMillis();
        }
    }

    private void initView() {
        login = (Button) findViewById(R.id.btn_login);
        username = (EditText) findViewById(R.id.et_username);
        password = (EditText) findViewById(R.id.et_password);
        imgView = (ImageView) findViewById(R.id.img_loading);
        animationDrawable = (AnimationDrawable) imgView.getDrawable();
    }

    private void addListener() {
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
    //手机储存地址，可视情况而改
    private static final String filePath = Environment.getExternalStorageDirectory() + "/storage/emulated/0/";
    private static final String fileName = "log.txt";
    private static IntentFilter dynamic_filter = new IntentFilter();

    //线程用于发送动态广播
    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case UPDATE_CONTENT:
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
    };

    //将数据写入txt文件
    private void inData(String context) {
        writeTxtToFile(context);
    }

    public void writeTxtToFile(String content) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = content + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    //生成文件
    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    //生成文件夹
    public static void makeRootDirectory(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }


    private void messagePart() {
        //获得单例类
        final CurrentUser currentUser = CurrentUser.getInstance(this);
        dynamic_filter.addAction(DYNAMIC);

        //网络是否连接
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() && currentUser.isLogan()) {
            //建立线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Guobao 修改
                    // 这里我把brocastRec的对象改到了BrocastRec里，做成了一个静态变量
                    // 在这个地方会被注册，在主页面的登出那里可以注销
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
                        inData(s.get(0));
                        inData(s.get(1));
                        Message message = new Message();
                        message.what = UPDATE_CONTENT;
                        message.obj = s;
                        handler.sendMessage(message);
                    }
                    //每隔0.5秒请求一次
                    handler.postDelayed(this, 500);
                }
            }).start();
        } else {
            Toast.makeText(Login.this, "当前没有可用网络！", Toast.LENGTH_SHORT).show();
        }
    }
}