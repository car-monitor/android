package android.vic.MapManager;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.vic.R;

import com.alibaba.fastjson.JSON;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MapManager {
    int mapUpdatePeriod;
    private BaiduMap baiduMap;
    private MapSDK mapSDK;
    private MapUpdaterThread mapUpdaterThread;
    private Handler handler;
    private Callback onCarClick;
    private Callback onNetworkError;    // 无法连接到后台时回调
    private Callback onParseError;      // 后台返回的API不能正常解析时回调，通常是服务端返回的API不符合文档
    private Callback onDataFirstLoad;    // 地图第一次加载完成时
    private Callback onDataLoad;        // 地图加载完成时

    public MapManager(BaiduMap baiduMap, Handler handler) {
        this.baiduMap = baiduMap;
        this.mapSDK = new MapSDK(baiduMap);
        this.handler = handler;
        this.mapUpdaterThread = new MapUpdaterThread(baiduMap, mapSDK);
        this.mapUpdatePeriod = 3000;
    }

    public void startMapUpdater() {

        // 这些set方法会调用mapUpdaterThread的set方法
        setOnCarClick(this.onCarClick);
        setOnDataFirstLoad(this.onDataFirstLoad);
        setOnDataLoad(this.onDataLoad);
        setOnNetworkError(this.onNetworkError);
        setOnParseError(this.onParseError);
        mapUpdaterThread.setPeriod(mapUpdatePeriod);
        mapUpdaterThread.start();
    }

    public void getMapCenterCity(final Callback callback) {
        MapStatus mapStatus = baiduMap.getMapStatus();
        final LatLng center = new LatLng(mapStatus.target.latitude, mapStatus.target.longitude);
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().get()
                        .url(String.format("http://api.map.baidu.com/geocoder/v2/?output=json&ak=kfGrUVqzN6Ohcx90Gdk246n8tAuVSnCs&location=%f,%f", center.latitude, center.longitude))
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

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.handleMessage(bundle);
                        }
                    });
                } catch (IOException e) {
                    final Bundle bundle = new Bundle();
                    bundle.putBoolean("status", false);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.handleMessage(bundle);
                        }
                    });
                }
            }
        }).start();
    }

    public void setMapCenterCity(City city) {
        if (city.equals(City.Guangzhou)) {
            mapSDK.centerView(new LatLng(23.16, 113.23));
        } else if (city.equals(City.Beijing)) {
            mapSDK.centerView(new LatLng(39.56, 116.24));
        }
    }

    public void setMapCenterCity(double longitude, double latitude) {
        mapSDK.centerView(new LatLng(latitude, longitude));
    }

    public Callback getOnCarClick() {
        return onCarClick;
    }

    public void setOnCarClick(Callback onCarClick) {
        this.onCarClick = onCarClick;
        mapUpdaterThread.setOnCarClick(new MapUpdaterThread.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                final Bundle mBundle = bundle;
                if (MapManager.this.onCarClick != null)
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MapManager.this.onCarClick.handleMessage(mBundle);
                        }
                    });
            }
        });
    }

    public Callback getOnNetworkError() {
        return onNetworkError;

    }

    public void setOnNetworkError(Callback onNetworkError) {
        this.onNetworkError = onNetworkError;
        mapUpdaterThread.setOnNetworkError(new MapUpdaterThread.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                final Bundle mBundle = bundle;
                if (MapManager.this.onNetworkError != null)
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MapManager.this.onNetworkError.handleMessage(mBundle);
                        }
                    });
            }
        });
    }

    public Callback getOnParseError() {
        return onParseError;
    }

    public void setOnParseError(Callback onParseError) {
        this.onParseError = onParseError;
        mapUpdaterThread.setOnParseError(new MapUpdaterThread.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                final Bundle mBundle = bundle;
                if (MapManager.this.onParseError != null)
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MapManager.this.onParseError.handleMessage(mBundle);
                        }
                    });
            }
        });
    }

    public Callback getOnDataFirstLoad() {
        return onDataFirstLoad;
    }

    public void setOnDataFirstLoad(Callback onDataFirstLoad) {
        this.onDataFirstLoad = onDataFirstLoad;
        mapUpdaterThread.setOnDataFirstLoad(new MapUpdaterThread.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                final Bundle mBundle = bundle;
                if (MapManager.this.onDataFirstLoad != null)
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MapManager.this.onDataFirstLoad.handleMessage(mBundle);
                        }
                    });
            }
        });
    }

    public Callback getOnDataLoad() {
        return onDataLoad;
    }

    public void setOnDataLoad(Callback onDataLoad) {
        this.onDataLoad = onDataLoad;

        mapUpdaterThread.setOnDataLoad(new MapUpdaterThread.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                final Bundle mBundle = bundle;
                if (MapManager.this.onDataLoad != null)
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MapManager.this.onDataLoad.handleMessage(mBundle);
                        }
                    });
            }
        });
    }

    public enum City {
        Guangzhou, Beijing
    }

    public interface Callback {
        void handleMessage(Bundle bundle);
    }
}

// 封装百度地图绘图方法


