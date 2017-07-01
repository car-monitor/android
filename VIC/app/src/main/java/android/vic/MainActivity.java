package android.vic;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private TextView     textView_City        = null;
    private DrawerLayout drawerLayout         = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 上面是模板自带的
        // 获取元素
        textView_City        = (TextView    )findViewById(R.id.city);
        ImageButton imageButton_icon = (ImageButton) findViewById(R.id.main_user);
        ImageButton message_icon = (ImageButton) findViewById(R.id.message);
        drawerLayout         = (DrawerLayout)findViewById(R.id.drawer_layout);

        View headerLayout = navigationView.getHeaderView(0);
        ImageButton imageButton_nav_icon = (ImageButton) headerLayout.findViewById(R.id.nav_bar_icon);
        Button button_nav_userName = (Button) headerLayout.findViewById(R.id.nav_bar_username);

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
            public void onClick(View v) {
                // TODO 跳转消息列表页面
            }
        });

        // TODO yzy在main页面的操作可以写到这里来
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

        // 其他情况下，双击退出
        if (timestamp == 0) {
            Toast.makeText(MainActivity.this, "再次点击返回键可退出", Toast.LENGTH_SHORT).show();
            timestamp = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - timestamp <= Toast.LENGTH_SHORT) {
            System.exit(0);
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
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://" + CurrentUser.IP + "logout");
                connection = (HttpURLConnection)url.openConnection();
                connection.setConnectTimeout(4000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Cookie", CurrentUser.getInstance(getApplicationContext()).getSessionID());
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
