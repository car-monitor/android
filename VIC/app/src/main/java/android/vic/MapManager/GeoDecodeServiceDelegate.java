package android.vic.MapManager;

import android.os.Bundle;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yangzy on 2017/7/2.
 */

class GeoDecodeServiceDelegate {
    static String API_KEY = "kfGrUVqzN6Ohcx90Gdk246n8tAuVSnCs";

    void decode(LatLng center, final Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().get()
                .url(String.format("http://api.map.baidu.com/geocoder/v2/?output=json&ak=%s&location=%f,%f", API_KEY, center.latitude, center.longitude))
                .build();
        try {
            Response response = client.newCall(request).execute();
            String response_json = response.body().string();
            Object document = Configuration.defaultConfiguration().jsonProvider().parse(response_json);

            String country = JsonPath.read(document, "$.result.addressComponent.country");
            String province = JsonPath.read(document, "$.result.addressComponent.province");
            String city = JsonPath.read(document, "$.result.addressComponent.city");
            String district = JsonPath.read(document, "$.result.addressComponent.district");
            String street = JsonPath.read(document, "$.result.addressComponent.street");


            final Bundle bundle = new Bundle();
            bundle.putString("country", country);
            bundle.putString("province", province);
            bundle.putString("city", city);
            bundle.putString("district", district);
            bundle.putString("street", street);

            callback.handleMessage(bundle);

        } catch (IOException e) {
            final Bundle bundle = new Bundle();
            bundle.putBoolean("status", false);

            callback.handleMessage(bundle);
        }
    }

    interface Callback {
        void handleMessage(Bundle bundle);
    }
}
