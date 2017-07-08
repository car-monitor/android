package android.vic;

import android.Manifest;

import android.app.Activity;
import android.app.Dialog;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;


import android.vic.MapManager.MapManager;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.TextureMapView;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import rx.functions.Action1;

/**
 * 主页面
 * Create by Guobao and Yzy
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView     textView_City = null;
    private DrawerLayout drawerLayout  = null;

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

        CurrentUser.activityMap.put("MainActivity", this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 上面是模板自带的
        // 获取元素
        textView_City                 = (TextView)    findViewById(R.id.city);
        drawerLayout                  = (DrawerLayout)findViewById(R.id.drawer_layout);
        ImageButton imageButton_icon  = (ImageButton) findViewById(R.id.main_user);
        ImageButton message_icon      = (ImageButton) findViewById(R.id.message);

        View headerLayout = navigationView.getHeaderView(0);
        ImageButton imageButton_nav_icon = (ImageButton) headerLayout.findViewById(R.id.nav_bar_icon);
        Button button_nav_userName = (Button) headerLayout.findViewById(R.id.nav_bar_username);

        if (CurrentUser.getInstance(this).isLogan())
            button_nav_userName.setText(CurrentUser.getInstance(this).getUsername());

        // 事件绑定
        View.OnClickListener toUserInfo = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShowUserInfo.class);
                startActivity(intent);

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
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ContextList.class);
                startActivity(intent);
            }
        });

        // ------
        // Author: 杨梓阳
        // Guobao 07.02 修改 把mapView的类型换成TextureMapView
        TextureMapView mapView = (TextureMapView) findViewById(R.id.bmapView);
        final MapManager mapManager = new MapManager(mapView.getMap(), new Handler());
        mapManager.setOnCarClick(new MapManager.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                Intent intent = new Intent(MainActivity.this, CarDetailActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }

        });
        // 无法连接到后台时回调，执行回调后线程MapUpdaterThread将sleep 10秒
        mapManager.setOnNetworkError(new MapManager.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                Toast.makeText(MainActivity.this, "无法连接到后台", Toast.LENGTH_LONG).show();
            }
        });
        // 后台返回的数据格式不正确时回调，执行回调后线程MapUpdaterThread将终止
        mapManager.setOnParseError(new MapManager.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                Toast.makeText(MainActivity.this, "后台返回的数据格式不正确", Toast.LENGTH_LONG).show();
            }
        });
        mapManager.setOnDataFirstLoad(new MapManager.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                mapManager.getMapCenterCity(new MapManager.Callback() {
                    @Override
                    public void handleMessage(Bundle bundle) {
                        textView_City.setText(bundle.getString("city"));
                    }
                });
            }
        });
        mapManager.setMapCenterCity(MapManager.City.Guangzhou);
        mapManager.startMapUpdater();
        // ------
    }

    @Override
    public void finish() {
        CurrentUser.activityMap.remove("MainActivity");
        super.finish();
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


    // 返回键事件
    private long timestamp = 0;
    @Override
    public void onBackPressed() {
        // 如果有弹窗，什么都不管
        if (dialog != null && dialog.isShowing()) {
            super.onBackPressed();
            return;
        }

        // 如果开着侧滑，关闭侧滑
        if (drawerLayout!= null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        // 双击退出
        Log.e("BACK", "CLICK");
        if (timestamp == 0) {
            Toast.makeText(MainActivity.this, "双击返回键可退出", Toast.LENGTH_SHORT).show();
            timestamp = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - timestamp <= 2000) {
            for (Map.Entry<String, Activity> entry: CurrentUser.activityMap.entrySet()) {
                if (entry.getValue() != null)
                    entry.getValue().finish();
            }
        } else {
            timestamp = System.currentTimeMillis();
        }
    }

    // 菜单各项的操作
    @SuppressWarnings("StatementWithEmptyBody")
    @Override

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_cars) {
            Intent intent= new Intent(MainActivity.this, DetailListActivity.class);
            intent.putExtra("type", DetailListActivity.TYPE_CAR);
            startActivity(intent);
        } else if (id == R.id.nav_drivers) {
            Intent intent= new Intent(MainActivity.this, DetailListActivity.class);
            intent.putExtra("type", DetailListActivity.TYPE_DRIVER);
            startActivity(intent);
        } else if (id == R.id.nav_bill) {
            Intent intent= new Intent(MainActivity.this, DetailListActivity.class);
            intent.putExtra("type", DetailListActivity.TYPE_ORDER);
            startActivity(intent);
        } else if (id == R.id.nav_exit) {
            // 登出
            Logout logoutTask = new Logout();
            logoutTask.execute();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private Dialog dialog = null;
    private class Logout extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setView(R.layout.logouting_dialog);
            dialog = builder.create();
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // 本地数据
            // 如果要改成服务器，请注释掉本段代码
            /*------------------------------------*/
            if (1 == 1) return true;
            /*------------------------------------*/

            HttpURLConnection connection = null;
            try {
                URL url = new URL(CurrentUser.IP + "logout");
                connection = (HttpURLConnection)url.openConnection();

                connection.setConnectTimeout(4000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Cookie", CurrentUser.getInstance(getApplicationContext()).getCookie());
                connection.connect();

                int statusCode = connection.getResponseCode();
                if (statusCode == 404) {
                    return false;
                } else {
                    String tmp;
                    String responseStr = "";
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((tmp = reader.readLine()) != null)
                        responseStr += tmp;
                    connection.disconnect();
                    JSONObject object = new JSONObject(responseStr);
                    return object.getInt("status") == 1;
                }
            } catch (MalformedURLException e) {
                Log.e("Main", "Logout URL is useless");
                e.printStackTrace();
            } catch (IOException e) {

                Log.e("Main", "Logout IO is false");
                e.printStackTrace();
            } catch (JSONException e) {
                Log.e("Main", "Response is not a json");

                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            if (dialog != null && dialog.isShowing()) {
                dialog.cancel();
            }
            if (status) {
                Toast.makeText(getApplicationContext(), "登出成功", Toast.LENGTH_SHORT).show();

                Login login = (Login)CurrentUser.activityMap.get("Login");
                if (BrocastRec.brocastRec != null) login.unregisterReceiver(BrocastRec.brocastRec);
                login.handler.removeCallbacks(login.runnable);

                CurrentUser.getInstance(getApplicationContext()).clearLoginInfo(getApplicationContext());
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "登出失败", Toast.LENGTH_SHORT).show();
                if (dialog != null && dialog.isShowing()) {
                    dialog.cancel();
                }
            }
        }
    }
}
