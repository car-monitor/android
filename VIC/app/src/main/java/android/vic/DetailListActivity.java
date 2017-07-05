package android.vic;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.vic.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DetailListActivity extends AppCompatActivity {

    public static int TYPE_CAR= 0;
    public static int TYPE_DRIVER= 1;
    public static int TYPE_ORDER= 2;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private ListViewAdapter adapter;
    private List<ListItem> list = new ArrayList();

    private int type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_list);
        Bundle bundle = getIntent().getExtras();
        type= bundle.getInt("type");
        swipeRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.SRL);
        listView= (ListView) findViewById(R.id.lv);
        adapter= new ListViewAdapter(this, list, type);
        list.add(new ListItem("a", "b", "c"));
        list.add(new ListItem("sd", "a", "fg"));
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent= null;
                if (type== TYPE_CAR) {
                    intent= new Intent(DetailListActivity.this, CarDetailActivity.class);
                    intent.putExtra("car_id", list.get(i).id);
                } else if (type== TYPE_DRIVER) {
                    intent= new Intent(DetailListActivity.this, DriverDetailActivity.class);
                    intent.putExtra("driver_id", list.get(i).id);
                } else if (type== TYPE_ORDER) {
                    intent= new Intent(DetailListActivity.this, OrderDetailActivity.class);
                    intent.putExtra("order_id", list.get(i).id);
                }
                startActivity(intent);

            }
        });

        new GetList().execute();

        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetList().execute();
            }
        });
    }

    private class GetList extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... integers) {

            HttpURLConnection connection= null;
            try {
                URL url= null;
                if (type== TYPE_CAR) {
                    url = new URL(CurrentUser.IP + "getcars");
                } else if (type== TYPE_DRIVER) {
                    url = new URL(CurrentUser.IP + "getusers?authority=1");
                } else if (type== TYPE_ORDER) {
                    url = new URL(CurrentUser.IP + "getorders");
                }

                connection= (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setRequestProperty("Cookie", CurrentUser.getInstance(getApplicationContext()).getSessionID());
                connection.connect();

                int statusCode= connection.getResponseCode();
                if (statusCode== 404) {
                    return false;
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
                        return false;
                    list.clear();
                    if (type== TYPE_CAR) {
                        JSONArray cars= obj.getJSONArray("cars");
                        for (int i= 0; i< cars.length(); i++) {
                            JSONObject car= cars.getJSONObject(i);
                            ListItem item= new ListItem(car.getString("id"), car.getString("carPlate"), null);
                            list.add(item);
                        }
                    } else if (type== TYPE_DRIVER) {
                        JSONArray drivers= obj.getJSONArray("users");
                        for (int i= 0; i< drivers.length(); i++) {
                            JSONObject driver= drivers.getJSONObject(i);
                            ListItem item= new ListItem(driver.getString("id"), driver.getString("username"), null);
                            list.add(item);
                        }
                    } else if (type== TYPE_ORDER) {
                        JSONArray orders= obj.getJSONArray("orderdetails");
                        for (int i= 0; i< orders.length(); i++) {
                            JSONObject order= orders.getJSONObject(i).getJSONObject("order");
                            ListItem item= new ListItem(order.getString("id"), order.getString("carID"), order.getString("driverId"));
                            list.add(item);
                        }
                    }

                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection!= null) {
                    connection.disconnect();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean Success) {
            if (!Success) {
                Toast.makeText(DetailListActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
            } else {
                adapter.notifyDataSetChanged();
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    private class ListViewAdapter extends BaseAdapter {
        private Context contex;
        private List<ListItem> list;
        private int item_type;

        public ListViewAdapter(Context c, List<ListItem> l, int type) {
            contex= c;
            list= l;
            item_type= type;
        }

        @Override
        public int getCount() {
            if (list== null)
                return 0;
            else
                return list.size();
        }

        @Override
        public Object getItem(int i) {
            if (list== null)
                return null;
            else
                return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view== null) {
                view= LayoutInflater.from(contex).inflate(R.layout.detail_list_item, null);
                viewHolder= new ViewHolder();
                viewHolder.image_view= (ImageView) view.findViewById(R.id.item_image);
                viewHolder.id_text= (TextView) view.findViewById(R.id.item_id);
                viewHolder.first_text= (TextView) view.findViewById(R.id.item_first);
                viewHolder.second_text= (TextView) view.findViewById(R.id.item_second);
                view.setTag(viewHolder);
            } else {
                viewHolder= (ViewHolder) view.getTag();
            }
            ListItem item= list.get(i);
            if (item_type== TYPE_CAR) {
                int resId= contex.getResources().getIdentifier("car", "mipmap", contex.getPackageName());
                viewHolder.image_view.setImageResource(resId);
                viewHolder.id_text.setText("id: "+ item.id);
                viewHolder.first_text.setText("车牌号: "+ item.id);
                viewHolder.second_text.setText("");
            } else if (item_type== TYPE_DRIVER) {
                int resId= contex.getResources().getIdentifier("user", "mipmap", contex.getPackageName());
                viewHolder.image_view.setImageResource(resId);
                viewHolder.id_text.setText("id: "+ item.id);
                viewHolder.first_text.setText("username: "+ item.first);
                viewHolder.second_text.setText("");
            } else if (item_type== TYPE_ORDER) {
                int resId= contex.getResources().getIdentifier("bill", "mipmap", contex.getPackageName());
                viewHolder.image_view.setImageResource(resId);
                viewHolder.id_text.setText("id: "+ item.id);
                viewHolder.first_text.setText("carid: "+ item.first);
                viewHolder.second_text.setText("driverid: "+ item.second);
            }
            return view;
        }

        private class ViewHolder {
            public ImageView image_view;
            public TextView id_text;
            public TextView first_text;
            public TextView second_text;
        }
    }

    private class ListItem {
        public ListItem(String _id, String _first, String _second) {
            id= _id;
            first= _first;
            second= _second;
        }
        public String id;
        public String first;
        public String second;
    }
}
