package android.vic;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import rx.functions.Action1;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private TextView     textView_City        = null;
    private ImageButton  imageButton_icon     = null;
    private ImageButton  message_icon         = null;
    private DrawerLayout drawerLayout         = null;
    private ImageButton  imageButton_nav_icon = null;
    private Button       button_nav_userName  = null;
    private MapView      mapView              = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ------
        // Author: 杨梓阳
        // 对不住了老铁，SDK必须在setContentView之前初始化
        requestPermission();
        SDKInitializer.initialize(getApplicationContext());
        // ------

        setContentView(R.layout.activity_main);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 上面是模板自带的
        // 获取元素
        textView_City        = (TextView    )findViewById(R.id.city);
        imageButton_icon     = (ImageButton )findViewById(R.id.main_user);
        message_icon         = (ImageButton )findViewById(R.id.message);
        drawerLayout         = (DrawerLayout)findViewById(R.id.drawer_layout);

        View headerLayout = navigationView.getHeaderView(0);
        imageButton_nav_icon = (ImageButton )headerLayout.findViewById(R.id.nav_bar_icon);
        button_nav_userName  = (Button      )headerLayout.findViewById(R.id.nav_bar_username);

        // 事件绑定
        View.OnClickListener toUserInfo = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 跳转个人信息页面
            }
        };
        imageButton_nav_icon.setOnClickListener(toUserInfo);
        button_nav_userName.setOnClickListener(toUserInfo);

        imageButton_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START, true);
            }
        });

        message_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 跳转消息列表页面
            }
        });

        // ------
        // Author: 杨梓阳
        MapView mapView = (MapView) findViewById(R.id.bmapView);
        final MapUpdaterThread mapUpdaterThread = new MapUpdaterThread(mapView.getMap());
        MapUpdaterThread.Callback onCarClick = new MapUpdaterThread.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                Intent intent = new Intent(MainActivity.this, CarDetailActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        };
        // 无法连接到后台时回调，执行回调后线程MapUpdaterThread将sleep 10秒
        MapUpdaterThread.Callback onNetworkError = new MapUpdaterThread.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                Toast.makeText(MainActivity.this, "", Toast.LENGTH_LONG).show();
            }
        };
        // 后台返回的数据格式不正确时回调，执行回调后线程MapUpdaterThread将终止
        MapUpdaterThread.Callback onParseError = new MapUpdaterThread.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                Toast.makeText(MainActivity.this, "", Toast.LENGTH_LONG).show();
            }
        };

        mapUpdaterThread.setOnCarClick(onCarClick);
        mapUpdaterThread.setOnNetworkError(onNetworkError);
        mapUpdaterThread.setOnParseError(onParseError);
        mapUpdaterThread.start();
        // ------
    }

    private void requestPermission() {
        final Handler handler = new Handler();
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                        if (!granted) {
                            Toast.makeText(MainActivity.this,
                                    "Permission Denied. App will finish in 3 secs...",
                                    Toast.LENGTH_SHORT).show();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 3000);
                        }
                    }
                });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // 菜单各项的操作
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_cars) {
            // TODO 跳转到车辆列表页面
        } else if (id == R.id.nav_drivers) {
            // TODO 跳转到驾驶员列表页面
        } else if (id == R.id.nav_bill) {
            // TODO 跳转到订单列表页面
        } else if (id == R.id.nav_exit) {
            // 登出
            Logout logoutTask = new Logout();
            logoutTask.execute();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class Logout extends AsyncTask<Void, Void, Boolean> {
        // TODO 未完成

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://" + CurrentUser.IP + "logout");
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                // connection.

            } catch (MalformedURLException e) {
                Log.e("Main", "Logout URL is useless");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            if (status) {
                Toast.makeText(getApplicationContext(), "登出成功", Toast.LENGTH_SHORT).show();
                CurrentUser.getInstance(getApplicationContext()).clearLoginInfo(getApplicationContext());
                // TODO 跳转到登录页面
                //Intent intent = new Intent(this, )

            }
        }
    }
}
