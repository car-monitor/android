package android.vic;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.AsyncListUtil;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

// Author:杨梓阳
// 2017.7.1
// 这个Activity用于演示点击后接收被点击车辆的数据的方法

// Modified By Jiabin Cai on2017/7/3
//从网络接收单个车辆数据

public class CarDetailActivity extends AppCompatActivity {

    private static int INDEX_CARPLATE_NUM= 0;
    private static int INDEX_CAR_TYPE= 1;
    private static int INDEX_BUY_TIME= 2;
    private static int INDEX_CARGO_CAPACITY= 3;
    private static int INDEX_ENGINE_NUM= 4;
    private static int INDEX_CAR_OWNER= 5;
    private static int INDEX_PASSENGER_NUM= 6;


    public TextView car_plate_num, car_type, buy_time, cargo_capacity, engine_num, car_owner, passenger_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        int car_id = Integer.parseInt(bundle.getString("car_id"));
        setContentView(R.layout.activity_car_detail);

        initialView();

        new GetCarDetail().execute(car_id);

    }

    public void initialView() {
        car_plate_num= (TextView) findViewById(R.id.car_plate_num);
        car_type= (TextView) findViewById(R.id.car_type);
        buy_time= (TextView) findViewById(R.id.buy_time);
        cargo_capacity= (TextView) findViewById(R.id.cargo_capacity);
        engine_num= (TextView) findViewById(R.id.engine_num);
        car_owner= (TextView) findViewById(R.id.car_owner);
        passenger_num= (TextView) findViewById(R.id.passenger_num);
    }

    private class GetCarDetail extends AsyncTask<Integer, Void, String[]> {

        @Override
        protected String[] doInBackground(Integer... integers) {

            HttpURLConnection connection= null;
            try {
                URL url = new URL(CurrentUser.IP + "getcar?id="+ integers[0]);
                connection= (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setRequestProperty("Cookie", CurrentUser.getInstance(getApplicationContext()).getSessionID());
                connection.connect();

                int statusCode= connection.getResponseCode();
                if (statusCode== 404) {
                    return null;
                } else {
                    InputStream in= connection.getInputStream();
                    BufferedReader reader= new BufferedReader(new InputStreamReader(in));
                    StringBuilder response= new StringBuilder();
                    String line;
                    while ((line= reader.readLine())!= null) {
                        response.append(line);
                    }
                    JSONObject obj= new JSONObject(response.toString());
                    if (obj.getInt("status") == 0)
                        return null;
                    JSONObject car= obj.getJSONObject("car");
                    String[] list= new String[7];
                    list[INDEX_CARPLATE_NUM]   = car.getString("carPlate");
                    list[INDEX_CAR_TYPE]       = car.getString("carType");
                    list[INDEX_BUY_TIME]       = car.getString("buyTime");
                    list[INDEX_CARGO_CAPACITY] = car.getString("cargoCapacity");
                    list[INDEX_ENGINE_NUM]     = car.getString("engineNo");
                    list[INDEX_CAR_OWNER]      = car.getString("owner");
                    list[INDEX_PASSENGER_NUM]  = car.getString("passengerNum");
                    return list;
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection!= null) {
                    connection.disconnect();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] list) {
            if (list== null) {
                Toast.makeText(CarDetailActivity.this, "车辆信息获取失败", Toast.LENGTH_SHORT).show();
            } else {
                car_plate_num.setText(list[INDEX_CARPLATE_NUM]);
                car_type.setText(list[INDEX_CAR_TYPE]);
                buy_time.setText(list[INDEX_BUY_TIME]);
                cargo_capacity.setText(list[INDEX_CARGO_CAPACITY]);
                engine_num.setText(list[INDEX_ENGINE_NUM]);
                car_owner.setText(list[INDEX_CAR_OWNER]);
                passenger_num.setText(list[INDEX_PASSENGER_NUM]);
            }


        }
    }
}
