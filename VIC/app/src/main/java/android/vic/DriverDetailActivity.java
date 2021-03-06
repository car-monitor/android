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

public class DriverDetailActivity extends AppCompatActivity {

    private static int INDEX_DRIVER_ID= 0;
    private static int INDEX_DEPARTMENT_ID= 1;
    private static int INDEX_UNIT_ID= 2;
    private static int INDEX_ADDRESS= 3;
    private static int INDEX_PHONE= 4;
    private static int INDEX_JOBNO= 5;


    public TextView driver_id, department_id, unit_id, address, phone, jobNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_detail);

        CurrentUser.activityMap.put("DriverDetailActivity", this);

        Bundle bundle = getIntent().getExtras();
        int driver_id = Integer.parseInt(bundle.getString("driver_id"));

        initialView();

        new GetDriverDetail().execute(driver_id);
    }

    @Override
    public void finish() {
        CurrentUser.activityMap.remove("DriverDetailActivity");
        super.finish();
    }

    public void initialView() {
        driver_id= (TextView) findViewById(R.id.driver_id);
        department_id= (TextView) findViewById(R.id.department_id);
        unit_id= (TextView) findViewById(R.id.unit_id);
        address= (TextView) findViewById(R.id.driver_address);
        phone= (TextView) findViewById(R.id.driver_phone);
        jobNo= (TextView) findViewById(R.id.jobNo);
    }

    private class GetDriverDetail extends AsyncTask<Integer, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Integer... integers) {

            // 本地数据
            // 如果要改成服务器，请注释掉本段代码
            /*------------------------------------*/
            ArrayList<String> list_= new ArrayList<>();
            list_.add(INDEX_DRIVER_ID, "id");
            list_.add(INDEX_DEPARTMENT_ID, "companyID");
            list_.add(INDEX_UNIT_ID, "appartmentID");
            list_.add(INDEX_ADDRESS, "address");
            list_.add(INDEX_PHONE, "phone");
            list_.add(INDEX_JOBNO, "jobNo");
            if (1 == 1) return list_;
            /*------------------------------------*/

            HttpURLConnection connection= null;
            try {
                URL url = new URL(CurrentUser.IP + "getuser/"+ integers[0]);
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
                    JSONObject user= obj.getJSONObject("user");
                    ArrayList<String> list= new ArrayList<>();
                    list.add(INDEX_DRIVER_ID, user.getString("id"));
                    list.add(INDEX_DEPARTMENT_ID, user.getString("companyID"));
                    list.add(INDEX_UNIT_ID, user.getString("appartmentID"));
                    list.add(INDEX_ADDRESS, user.getString("address"));
                    list.add(INDEX_PHONE, user.getString("phone"));
                    list.add(INDEX_JOBNO, user.getString("jobNo"));
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
                Toast.makeText(DriverDetailActivity.this, "驾驶员信息获取失败", Toast.LENGTH_SHORT).show();
            } else {
                driver_id.setText(list.get(INDEX_DRIVER_ID));
                department_id.setText(list.get(INDEX_DEPARTMENT_ID));
                unit_id.setText(list.get(INDEX_UNIT_ID));
                address.setText(list.get(INDEX_ADDRESS));
                phone.setText(list.get(INDEX_PHONE));
                jobNo.setText(list.get(INDEX_JOBNO));
            }

        }
    }
}
