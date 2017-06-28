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

public class DriverList extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_list);

        Bundle bundle = getIntent().getExtras();
        final ArrayList<String> ss = bundle.getStringArrayList("driver");

        ListView Driver_list = (ListView)findViewById(R.id.driver_list);
        List<Map<String, Object>> data = new ArrayList<>();
        if (ss.size() != 0) {
            int i = 0;
            do {
                Map<String, Object> temp = new LinkedHashMap<>();
                temp.put("driver", ss.get(i++));
                data.add(temp);
            } while(i < ss.size());
            final SimpleAdapter simpleAdapter = new SimpleAdapter(this, data, R.layout.driver_item,
                    new String[] {"driver"}, new int[] {R.id.drivers});
            Driver_list.setAdapter(simpleAdapter);
        }

        Driver_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                Intent intent = new Intent(DriverList.this, Detail.class);
                Bundle bundle = new Bundle();
                bundle.putString("car", "");
                bundle.putString("driver", ss.get(i));
                bundle.putString("order", "");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
}