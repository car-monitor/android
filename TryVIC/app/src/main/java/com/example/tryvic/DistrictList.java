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

public class DistrictList extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.district_list);

        Bundle bundle = getIntent().getExtras();
        final ArrayList<String> ss = bundle.getStringArrayList("district");

        ListView District_list = (ListView)findViewById(R.id.district_list);
        List<Map<String, Object>> data = new ArrayList<>();
        if (ss.size() != 0) {
            int i = 0;
            do {
                Map<String, Object> temp = new LinkedHashMap<>();
                temp.put("district", ss.get(i++));
                data.add(temp);
            } while(i < ss.size());
            final SimpleAdapter simpleAdapter = new SimpleAdapter(this, data, R.layout.district_item,
                    new String[] {"district"}, new int[] {R.id.districts});
            District_list.setAdapter(simpleAdapter);
        }

        District_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                ArrayList<String> strings = new ArrayList<String>();
                /*
                等待后台站点信息，将该区域所有订单放入strings中
                 */
                Intent intent = new Intent(DistrictList.this, OrderList.class);
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("order", strings);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
}