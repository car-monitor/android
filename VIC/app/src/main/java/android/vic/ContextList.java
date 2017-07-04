package android.vic;

import android.content.Intent;
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
    private static final String filePath = Environment.getExternalStorageDirectory() + "/storage/emulated/0/";
    private static final String fileName = "log.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.context_list);

        ListView Context_list = (ListView)findViewById(R.id.context_list);
        final List<Map<String, Object>> data = new ArrayList<>();
        final List<Map<String, Object>> _data = new ArrayList<>();
        //一行一行地读取txt文件数据
        File file = new File(filePath+fileName);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                //将读取的标题String加到data里面
                Map<String, Object> temp = new LinkedHashMap<>();
                temp.put("title", tempString);
                data.add(temp);
                //将读取的内容String加到_data里面，但不显示出来
                tempString = reader.readLine();
                Map<String, Object> _temp = new LinkedHashMap<>();
                _temp.put("content", tempString);
                _data.add(_temp);
            }
            reader.close();
            final SimpleAdapter simpleAdapter = new SimpleAdapter(this, data, R.layout.context_item,
                    new String[] {"title"}, new int[] {R.id.contexts});
            Context_list.setAdapter(simpleAdapter);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
}