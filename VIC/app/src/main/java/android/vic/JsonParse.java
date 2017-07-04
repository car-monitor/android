package android.vic;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 请求消息，网络请求和解析
 * Created by 朱彦儒 on 2017/7/3 0003.
 */

public class JsonParse {
    // 解析部分
    public static List<Contexts> getListContexts(Context context) throws Exception {
        List<Contexts> clists = new ArrayList<>();
        byte[] data = readParse(context);
        JSONObject jsonObject = new JSONObject(new String(data));
        String string = jsonObject.getString("infos");
        JSONArray array = new JSONArray(string);
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            int type = item.getInt("type");
            String title = item.getString("title");
            String content = item.getString("content");
            clists.add(new Contexts(type, title, content));
        }
        return clists;
    }

    // 网络请求部分
    public static byte[] readParse(Context context) throws Exception {
        CurrentUser currentUser = CurrentUser.getInstance(context);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] data = new byte[50000];
        int len;
        URL url = new URL(CurrentUser.IP + "getmessage");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("cookie", currentUser.getSessionID());
        InputStream inputStream = connection.getInputStream();
        while (true) {
            len = inputStream.read(data);
            if (len != -1) {
                outputStream.write(data, 0, len);
            } else {
                break;
            }
        }
        inputStream.close();
        return outputStream.toByteArray();
    }
}
