package com.example.administrator.carmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/6/28 0028.
 */

public class CarList extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_list);

        Bundle bundle = getIntent().getExtras();
        final ArrayList<String> ss = bundle.getStringArrayList("car");

        ListView Car_list = (ListView)findViewById(R.id.car_list);
        List<Map<String, Object>> data = new ArrayList<>();
        if (ss.size() != 0) {
            int i = 0;
            do {
                Map<String, Object> temp = new LinkedHashMap<>();
                temp.put("car", ss.get(i++));
                data.add(temp);
            } while(i < ss.size());
            final SimpleAdapter simpleAdapter = new SimpleAdapter(this, data, R.layout.car_item,
                    new String[] {"car"}, new int[] {R.id.cars});
            Car_list.setAdapter(simpleAdapter);
        }

        Car_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                Intent intent = new Intent(CarList.this, Detail.class);
                Bundle bundle = new Bundle();
                bundle.putString("car", ss.get(i));
                bundle.putString("driver", "");
                bundle.putString("order", "");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
}
