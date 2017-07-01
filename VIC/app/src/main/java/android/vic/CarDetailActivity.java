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
        int id = bundle.getInt("id");
        Toast.makeText(CarDetailActivity.this, "CarId:" + Integer.toString(id), Toast.LENGTH_LONG).show();
        setContentView(R.layout.activity_new);
    }
}
