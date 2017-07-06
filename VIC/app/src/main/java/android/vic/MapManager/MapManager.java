package android.vic.MapManager;

import android.os.Bundle;
import android.os.Handler;

import com.baidu.mapapi.map.BaiduMap;


public class MapManager {
    private int mapUpdatePeriod;
    private MapSDK mapSDK;
    private MapUpdaterThread mapUpdaterThread;
    private Handler handler;
    private Callback onCarClick;
    private Callback onNetworkError;    // 无法连接到后台时回调
    private Callback onParseError;      // 后台返回的API不能正常解析时回调，通常是服务端返回的API不符合文档
    private Callback onDataFirstLoad;    // 地图第一次加载完成时
    private Callback onDataLoad;        // 地图加载完成时

    public MapManager(BaiduMap baiduMap, Handler handler) {
        this.mapSDK = new MapSDK(baiduMap);
        this.handler = handler;
        this.mapUpdaterThread = new MapUpdaterThread(mapSDK);
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
        final LatLng center = mapSDK.getMapCenter();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new GeoDecodeServiceDelegate().decode(center, new GeoDecodeServiceDelegate.Callback() {
                    @Override
                    public void handleMessage(Bundle bundle) {
                        final Bundle mBundle = bundle;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.handleMessage(mBundle);
                            }
                        });
                    }
                });
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


