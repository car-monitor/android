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
    private static int INDEX_STARTTIME= 5;
    private static int INDEX_ENDTIME= 6;
    private static int INDEX_NAME1= 7;
    private static int INDEX_PHONE1= 8;
    private static int INDEX_ADDRESS1= 9;
    private static int INDEX_NAME2= 11;
    private static int INDEX_PHONE2= 12;
    private static int INDEX_ADDRESS2= 13;



    public TextView car_id, driver_id, order_id, place_of_departure, destinattion, starttime, endtime, addressorName,
            addressorPhone, addressorAddress, addresseeName, addresseePhone, addresseeAddress;

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
        starttime= (TextView) findViewById(R.id.start_time);
        endtime= (TextView) findViewById(R.id.end_time);
        addressorName= (TextView) findViewById(R.id.addressorName);
        addressorPhone= (TextView) findViewById(R.id.addressorPhone);
        addressorAddress= (TextView) findViewById(R.id.addressorAddress);
        addresseeName= (TextView) findViewById(R.id.addresseeName);
        addresseePhone= (TextView) findViewById(R.id.addresseePhone);
        addresseeAddress= (TextView) findViewById(R.id.addresseeAddress);
    }

    private class GetOrderDetail extends AsyncTask<Integer, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Integer... integers) {

            // 本地数据
            // 如果要改成服务器，请注释掉本段代码
            /*------------------------------------*/
            ArrayList<String> list_= new ArrayList<>();
            list_.add(INDEX_CAR_ID, "carID");
            list_.add(INDEX_DRIVER_ID, "driverId");
            list_.add(INDEX_ORDER_ID, "id");
            list_.add(INDEX_PLACE_OF_DEPARTURE, "startLongitude"+ ", "+ "startLatitude");
            list_.add(INDEX_DESTINATION, "endLongitude"+ ", "+ "endLatitude");
            list_.add(INDEX_STARTTIME, "startTime");
            list_.add(INDEX_ENDTIME, "endTime");
            list_.add(INDEX_NAME1, "addressorName");
            list_.add(INDEX_NAME2, "addresseeName");
            list_.add(INDEX_PHONE1, "addressorPhone");
            list_.add(INDEX_PHONE2, "addresseePhone");
            list_.add(INDEX_ADDRESS1, "addressorAddress");
            list_.add(INDEX_ADDRESS2, "addresseeAddress");
            if (1 == 1) return list_;
            /*------------------------------------*/

            HttpURLConnection connection= null;
            try {
                URL url = new URL(CurrentUser.IP + "getorder/"+ integers[0]);
                connection= (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setRequestProperty("Cookie", CurrentUser.getInstance(getApplicationContext()).getCookie());
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
                    list.add(INDEX_PLACE_OF_DEPARTURE, order.getString("startLongitude")+ ", "+ order.getString("startLatitude"));
                    list.add(INDEX_DESTINATION, order.getString("endLongitude")+ ", "+ order.getString("endLatitude"));
                    list.add(INDEX_STARTTIME, order.getString("startTime"));
                    list.add(INDEX_ENDTIME, order.getString("endTime"));
                    list.add(INDEX_NAME1, order.getString("addressorName"));
                    list.add(INDEX_NAME2, order.getString("addresseeName"));
                    list.add(INDEX_PHONE1, order.getString("addressorPhone"));
                    list.add(INDEX_PHONE2, order.getString("addresseePhone"));
                    list.add(INDEX_ADDRESS1, order.getString("addressorAddress"));
                    list.add(INDEX_ADDRESS2, order.getString("addresseeAddress"));
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
                starttime.setText(list.get(INDEX_DESTINATION));
                endtime.setText(list.get(INDEX_DESTINATION));
                addressorName.setText(list.get(INDEX_NAME1));
                addressorPhone.setText(list.get(INDEX_PHONE1));
                addressorAddress.setText(list.get(INDEX_ADDRESS1));
                addresseeName.setText(list.get(INDEX_NAME2));
                addresseePhone.setText(list.get(INDEX_PHONE2));
                addresseeAddress.setText(list.get(INDEX_ADDRESS2));
            }

        }
    }
}
