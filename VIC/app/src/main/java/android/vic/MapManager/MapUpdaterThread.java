package android.vic.MapManager;

import android.os.Bundle;

import com.baidu.mapapi.map.BaiduMap;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * 地图车辆数据的远程获取 & 异步加载
 * Created by yangzy on 2017/6/30.
 */

// 顶层模块，调用各模块实现地图更新
class MapUpdaterThread extends Thread {
    private int period;
    private BaiduMap baiduMap;
    private MapSDK mapSDK;
    private Callback onCarClick = null;
    private Callback onNetworkError = null;    // 无法连接到后台时回调
    private Callback onParseError = null;      // 后台返回的API不能正常解析时回调，通常是服务端返回的API不符合文档
    private Callback onDataFirstLoad = null;    // 地图第一次加载完成时
    private Callback onDataLoad = null;        // 地图加载完成时

    MapUpdaterThread(BaiduMap baiduMap, MapSDK mapSDK) {
        this.baiduMap = baiduMap;
        this.mapSDK = mapSDK;
        period = 3000;
    }

    @Override
    public void run() {
        RouteBuilder routeBuilder = new RouteBuilder(mapSDK, new Route.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                if (MapUpdaterThread.this.onCarClick != null)
                    MapUpdaterThread.this.onCarClick.handleMessage(bundle);
            }
        });
        RouteDBDelegate routeDBDelegate = new RouteDBDelegateStub();
        RouteListManager routeListManager = new RouteListManager(getAllRouteInfo(routeDBDelegate), routeBuilder);

        // 每3s从后台服务器获得车辆位置数据，并加载到地图上
        Boolean firstLoad = true;
        while (true) {
            for (Route route : routeListManager.getActiveRoutes()) {
                route.removeSelf();
                route.drawSelf();
            }

            if (firstLoad) {
                firstLoad = false;
                if (onDataFirstLoad != null)
                    onDataFirstLoad.handleMessage(new Bundle());
            }
            if (onDataLoad != null)
                onDataLoad.handleMessage(new Bundle());

            routeListManager.updateRouteInfo(getAllRouteInfo(routeDBDelegate));
            mSleep(period);
        }
    }

    private void mSleep(long millisecond) {
        try {
            sleep(millisecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<RouteInfo> getAllRouteInfo(RouteDBDelegate routeDBDelegate) {
        while (true) {
            try {
                return routeDBDelegate.getAllRouteInfo();
            } catch (IOException e) {
                if (onNetworkError != null)
                    onNetworkError.handleMessage(new Bundle());
                mSleep(10000);
            } catch (ParseException e) {
                if (onParseError != null)
                    onParseError.handleMessage(new Bundle());
                this.interrupt();
            }
        }
    }

    public Callback getOnCarClick() {
        return onCarClick;
    }

    public void setOnCarClick(Callback onCarClick) {
        this.onCarClick = onCarClick;
    }

    public Callback getOnNetworkError() {
        return onNetworkError;
    }

    public void setOnNetworkError(Callback onNetworkError) {
        this.onNetworkError = onNetworkError;
    }

    public Callback getOnParseError() {
        return onParseError;
    }

    public void setOnParseError(Callback onParseError) {
        this.onParseError = onParseError;
    }

    public Callback getOnDataFirstLoad() {
        return onDataFirstLoad;
    }

    public void setOnDataFirstLoad(Callback onDataFirstLoad) {
        this.onDataFirstLoad = onDataFirstLoad;
    }

    public Callback getOnDataLoad() {
        return onDataLoad;
    }

    public void setOnDataLoad(Callback onDataLoad) {
        this.onDataLoad = onDataLoad;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    interface Callback {
        void handleMessage(Bundle bundle);
    }
}
