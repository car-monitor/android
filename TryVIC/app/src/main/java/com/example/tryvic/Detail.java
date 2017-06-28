package com.example.administrator.carmonitor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/6/28 0028.
 */

public class Detail extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_main);

        String s;
        int flag;
        Bundle bundle = getIntent().getExtras();
        if (bundle.getString("car").equals("")) {
            if (bundle.getString("driver").equals("")) {
                s = bundle.getString("order");
                flag = 3;
            } else {
                s = bundle.getString("driver");
                flag = 2;
            }
        } else {
            s = bundle.getString("car");
            flag = 1;
        }

        TextView CarID = (TextView)findViewById(R.id.carID);
        TextView DriverID = (TextView)findViewById(R.id.driverID);
        TextView OrderID = (TextView)findViewById(R.id.orderID);
        TextView Time = (TextView)findViewById(R.id.time);

        switch (flag) {
            case 1:
                /*
                等待车辆ID信息用于和s匹配，然后再setText
                 */
                break;
            case 2:
                /*
                等待驾驶员ID信息用于和s匹配，然后再setText
                 */
                break;
            case 3:
                /*
                等待订单ID信息用于和s匹配，然后再setText
                 */
                break;
            default:
                break;
        }
    }
}
