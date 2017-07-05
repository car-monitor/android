package android.vic;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.vic.R;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by caijiabin on 2017/7/3.
 * 运单详情页面
 */

public class OrderDetailActivity extends AppCompatActivity {

    private static int INDEX_CAR_ID= 0;
    private static int INDEX_DRIVER_ID= 1;
    private static int INDEX_ORDER_ID= 2;
    private static int INDEX_PLACE_OF_DEPARTURE= 3;
    private static int INDEX_DESTINATION= 4;
    private static int INDEX_ROUTE_INFO= 5;
    private static int INDEX_KEY_POINT= 6;
    private static int INDEX_ORDER_TIME= 7;


    public TextView car_id, driver_id, order_id, place_of_departure, destinattion, route_info, key_point, order_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        int order_id = Integer.parseInt(bundle.getString("order_id"));
        setContentView(R.layout.activity_order_detail);

        initialView();

        new GetOrderDetail().execute(order_id);

    }

    public void initialView() {
        car_id= (TextView) findViewById(R.id.car_id);
        driver_id= (TextView) findViewById(R.id.driver_id);
        order_id= (TextView) findViewById(R.id.order_id);
        place_of_departure= (TextView) findViewById(R.id.place_of_departure);
        destinattion= (TextView) findViewById(R.id.destination);
        route_info= (TextView) findViewById(R.id.route_info);
        key_point= (TextView) findViewById(R.id.key_point);
        order_time= (TextView) findViewById(R.id.order_time);
    }

    private class GetOrderDetail extends AsyncTask<Integer, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Integer... integers) {

            HttpURLConnection connection= null;
            try {
                URL url = new URL(CurrentUser.IP + "getorder?id="+ integers[0]);
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
                    JSONObject order= obj.getJSONObject("order");
                    ArrayList<String> list= new ArrayList<>();
                    list.add(INDEX_CAR_ID, order.getString("carID"));
                    list.add(INDEX_DRIVER_ID, order.getString("driverId"));
                    list.add(INDEX_ORDER_ID, order.getString("id"));
                    list.add(INDEX_PLACE_OF_DEPARTURE, "------");
                    list.add(INDEX_DESTINATION, "------");
                    list.add(INDEX_ROUTE_INFO, "------");
                    list.add(INDEX_KEY_POINT, "------");
                    list.add(INDEX_ORDER_TIME, "-----");
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
        protected void onPostExecute(ArrayList<String> list) {
            if (list== null) {
                Toast.makeText(OrderDetailActivity.this, "订单信息获取失败", Toast.LENGTH_SHORT).show();
            } else {
                car_id.setText(list.get(INDEX_CAR_ID));
                driver_id.setText(list.get(INDEX_DRIVER_ID));
                order_id.setText(list.get(INDEX_ORDER_ID));
                place_of_departure.setText(list.get(INDEX_PLACE_OF_DEPARTURE));
                destinattion.setText(list.get(INDEX_DESTINATION));
                route_info.setText(list.get(INDEX_ROUTE_INFO));
                key_point.setText(list.get(INDEX_KEY_POINT));
                order_time.setText(list.get(INDEX_ORDER_TIME));
            }

        }
    }
}