package android.vic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

// 用于测试点击地图中的车辆后跳转的Activity
public class CarDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        int car_id = bundle.getInt("car_id");
        int driver_id = bundle.getInt("driver_id");
        int order_id = bundle.getInt("order_id");
        Toast.makeText(CarDetailActivity.this,
                String.format("car_id:%d\ndriver_id:%d\norder_id:%d", car_id, driver_id, order_id),
                Toast.LENGTH_LONG).show();
        setContentView(R.layout.activity_new);
    }
}
