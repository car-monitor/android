package android.vic;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * 保存用户信息的单例类
 * Created by Guobao Xu on 2017/6/30.
 */

class CurrentUser {
    private CurrentUser() {

    }

    static CurrentUser getInstance() {
        if (currentUser == null)
            currentUser = new CurrentUser();
        return currentUser;
    }

    JSONObject getCacheObject() {
        // 已经读取过文件并缓存
        if (cacheObject != null)
            return cacheObject;

        // 检查文件是否存在
        File cacheFile = new File("assets/cache.json");
        if (!cacheFile.exists()) {
            Log.i("Cache", "File is not found, as mean that user has not logan");
            cacheObject = null;
        } else try {
            // 读取整个文件
            String jsonStr = "";
            String tmp;
            BufferedReader reader = new BufferedReader(new FileReader("cache.json"));
            while ((tmp = reader.readLine()) != null)
                jsonStr += tmp;
            reader.close();
            cacheObject = new JSONObject(jsonStr);
        } catch (FileNotFoundException e) {
            cacheObject = null;
            Log.e("FileIO", "cache.json is not exits");
            e.printStackTrace();
        } catch (JSONException e) {
            cacheObject = null;
            Log.e("FileIO", "Cannot parse to a json object");
            e.printStackTrace();
        } catch (IOException e) {
            cacheObject = null;
            Log.e("FileIO", "There is a IO Exception while \"readline()\"");
            e.printStackTrace();
        }
        return cacheObject;
    }

    public boolean setCacheObject(JSONObject cacheObject, Context context) {
        this.cacheObject = cacheObject;
        // 直接覆盖式写入
        try {
            FileOutputStream outputStream = context.openFileOutput("cache.json", Context.MODE_PRIVATE);
            outputStream.write(cacheObject.toString().getBytes());
            outputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            Log.e("FileIO", "Cannot open the file cache.json.");
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.e("FileIO", "Cannot write to the file cache.json.");
            e.printStackTrace();
            return false;
        }
    }

    private static CurrentUser currentUser = null;
    private JSONObject cacheObject = null;
}
