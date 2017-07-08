package android.vic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息列表页面
 * Created by 朱彦儒 on 2017/7/2 0002.
 */

public class ContextList extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.context_list);

        CurrentUser.activityMap.put("ContextList", this);
        
        final SharedPreferences sp = getSharedPreferences("Context", MODE_PRIVATE);
        final Count count = (Count)getApplicationContext();

        int _count = count.getCount();
        ListView Context_list = (ListView)findViewById(R.id.context_list);
        final List<Map<String, Object>> data = new ArrayList<>();
        final List<Map<String, Object>> _data = new ArrayList<>();
        for(int i = 0; i < _count; i++) {
            Map<String, Object> temp = new LinkedHashMap<>();
            temp.put("title", sp.getString("title"+(i+""), "default"));
            data.add(temp);
            Map<String, Object> _temp = new LinkedHashMap<>();
            _temp.put("content", sp.getString("content"+(i+""), "default"));
            _data.add(_temp);
        }
        final SimpleAdapter simpleAdapter = new SimpleAdapter(this, data, R.layout.context_item,
                new String[] {"title"}, new int[] {R.id.contexts});
        Context_list.setAdapter(simpleAdapter);

        Context_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                Intent intent = new Intent(ContextList.this, ContextActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("title", data.get(i).get("title").toString());
                bundle.putString("content", _data.get(i).get("content").toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public void finish() {
        CurrentUser.activityMap.remove("ContextList");
        super.finish();
    }
}
