package android.vic;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;


// 连接远程WebService，获取数据
interface RouteDBDelegate {
    List<RouteInfo> getAllRouteInfo() throws IOException, ParseException;
}

/**
 * 地图车辆数据的远程获取 & 异步加载
 * Created by yangzy on 2017/6/30.
 */

// 顶层模块，调用各模块实现地图更新
class MapUpdaterThread extends Thread {
    private final int period = 5000;
    private BaiduMap baiduMap;
    private Activity activity;
    private Handler handler;

    MapUpdaterThread(BaiduMap baiduMap, Activity activity, Handler handler) {
        this.baiduMap = baiduMap;
        this.activity = activity;
        this.handler = handler;
    }

    @Override
    public void run() {
        MapSDK mapSDK = new MapSDK(baiduMap);
        RouteBuilder routeBuilder = new RouteBuilder(mapSDK, new Route.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                Intent intent = new Intent(activity, CarDetailActivity.class);
                intent.putExtras(bundle);
                activity.startActivity(intent);
            }
        });
        RouteDBDelegate routeDBDelegate = new RouteDBDelegateStub();
        RouteListManager routeListManager = new RouteListManager(getAllRouteInfo(routeDBDelegate), routeBuilder);

        // 每3s从后台服务器获得车辆位置数据，并加载到地图上
        while (true) {
            for (Route route : routeListManager.getActiveRoutes()) {
                route.removeSelf();
                route.drawSelf();
            }
            routeListManager.updateRouteInfo(getAllRouteInfo(routeDBDelegate));
            mSleep(3000);
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
        for (int i = 0; i < 1; i++) {
            try {
                return routeDBDelegate.getAllRouteInfo();
            } catch (IOException e) {
                i--;
                showToast("Fail to connect server\nTry again in 10 seconds", Toast.LENGTH_LONG);
                mSleep(10000);
            } catch (ParseException e) {
                showToast("Fail to resolve data from server\nContact administrator", Toast.LENGTH_LONG);
                this.interrupt();
            }
        }

        return null;    // unreachable
    }

    private void showToast(final String text, final int length) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, text, length).show();
            }
        });
    }
}

// 职责：调用地图SDK绘出自己
class Route {
    private static BitmapDescriptor trunkImage = BitmapDescriptorFactory
            .fromResource(R.drawable.truck);
    private static int[] pathColor = {0xAADCAB3D, Color.LTGRAY, Color.YELLOW};
    private RouteInfo routeInfo;
    private MapSDK mapSDK;
    private Route.Callback onCarClicked;

    private Integer path_marker_id = null;
    private Integer car_marker_id = null;

    Route(RouteInfo routeInfo, MapSDK mapSDK, Route.Callback onCarClicked) {
        this.routeInfo = routeInfo;
        this.mapSDK = mapSDK;
        this.onCarClicked = onCarClicked;
    }

    public RouteInfo getRouteInfo() {
        return routeInfo;
    }

    public void setRouteInfo(RouteInfo routeInfo) {
        this.routeInfo = routeInfo;
    }

    void drawSelf() {
        List<RouteSectionInfo> sectionList = routeInfo.getRouteSectionInfoList();

        for (int i = 0; i < sectionList.size(); i++) {
            RouteSectionInfo section = sectionList.get(i);
            List<LatLng> path = section.getPath();
            List<LatLng> mPath = new LinkedList<>();    // mPath比path加入了下一段路径的起点
            mPath.addAll(path);

            if (i == sectionList.size() - 1) {
                LatLng curPoint = path.get(path.size() - 1);
                car_marker_id = mapSDK.drawPoint(curPoint, trunkImage, new MapSDK.Callback() {
                    @Override
                    public void handleMessage(Bundle bundle) {
                        bundle.putInt("id", Route.this.routeInfo.getId());
                        Route.this.onCarClicked.handleMessage(bundle);
                    }
                });
            } else {
                // 补上两段折线间的空白
                List<LatLng> nextPath = sectionList.get(i + 1).getPath();
                mPath.add(nextPath.get(0));
            }

            path_marker_id = mapSDK.drawPolyline(mPath, pathColor[i % pathColor.length]);
        }
    }

    void removeSelf() {
        if (path_marker_id != null)
            mapSDK.removePath(path_marker_id);
        if (car_marker_id != null)
            mapSDK.removePoint(car_marker_id);
    }

    interface Callback {
        void handleMessage(Bundle bundle);
    }
}

// 封装百度地图绘图方法
class MapSDK {
    private BaiduMap baiduMap;  // 地图

    // 下面的属性用于追踪已绘制的元素（如车辆点、车辆路径）
    private SparseArray<Overlay> pointMap;
    private SparseArray<Overlay> polylineMap;
    private SparseArray<Callback> callbackMap;

    MapSDK(BaiduMap baiduMap) {
        this.pointMap = new SparseArray<>();
        this.polylineMap = new SparseArray<>();
        this.callbackMap = new SparseArray<>();
        this.baiduMap = baiduMap;
        this.baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Bundle marker_bundle = marker.getExtraInfo();
                int marker_id = marker_bundle.getInt("marker_id");
                callbackMap.get(marker_id).handleMessage(marker_bundle);
                return true;
            }
        });
    }

    int drawPolyline(List<LatLng> path, int color) {
        int marker_id = NoRepeatRandom.gen();
        Bundle bundle = new Bundle();
        bundle.putInt("marker_id", marker_id);

        OverlayOptions ooPolyline = new PolylineOptions().width(20).extraInfo(bundle)
                .color(color).points(path);
        Overlay overlay = baiduMap.addOverlay(ooPolyline);

        polylineMap.put(marker_id, overlay);

        return marker_id;
    }

    int drawPolyline(List<LatLng> path, int color, MapSDK.Callback callback) {
        int marker_id = drawPolyline(path, color);
        callbackMap.put(marker_id, callback);
        return marker_id;
    }

    int drawPoint(LatLng point, BitmapDescriptor pointImage, Callback OnClick) {
        int marker_id = NoRepeatRandom.gen();
        Bundle bundle = new Bundle();
        bundle.putInt("marker_id", marker_id);

        OverlayOptions ooDot = new MarkerOptions().icon(pointImage).position(point).extraInfo(bundle);
        Overlay overlay = baiduMap.addOverlay(ooDot);

        pointMap.put(marker_id, overlay);
        callbackMap.put(marker_id, OnClick);

        return marker_id;
    }

    boolean removePoint(int marker_id) {
        Overlay overlay = pointMap.get(marker_id);
        Callback callback = callbackMap.get(marker_id);
        if (overlay == null || callback == null)
            return false;

        overlay.remove();
        pointMap.remove(marker_id);
        callbackMap.remove(marker_id);

        return true;
    }

    boolean removePath(int marker_id) {
        Overlay overlay = polylineMap.get(marker_id);
        if (overlay == null)
            return false;

        overlay.remove();
        polylineMap.remove(marker_id);

        return true;
    }

    interface Callback {
        void handleMessage(Bundle bundle);
    }
}

// 测试用
class RouteDBDelegateStub implements RouteDBDelegate {
    private final String hostName;
    int timer = 0;

    RouteDBDelegateStub() {
        this.hostName = "localhost";
    }

    public List<RouteInfo> getAllRouteInfo() throws IOException, ParseException {
        List<RouteInfo> routeInfoList = new LinkedList<>();
        List<LatLng> pathList1 = new LinkedList<>();
        List<LatLng> pathList2 = new LinkedList<>();
        List<LatLng> pathList3 = new LinkedList<>();
        List<Timestamp> timestampList1 = new LinkedList<>();
        List<Timestamp> timestampList2 = new LinkedList<>();
        List<Timestamp> timestampList3 = new LinkedList<>();
        List<RouteSectionInfo> sectionList1 = new LinkedList<>();
        List<RouteSectionInfo> sectionList2 = new LinkedList<>();

        pathList1.add(new LatLng(39.921634, 116.413039));
        pathList1.add(new LatLng(39.921781, 116.417611));
        pathList2.add(new LatLng(39.924843, 116.417566));
        pathList2.add(new LatLng(39.925037, 116.423926));
        timestampList1.add(new Timestamp(System.currentTimeMillis() - 5 * 60 * 1000));
        timestampList1.add(new Timestamp(System.currentTimeMillis() - 4 * 60 * 1000));
        timestampList1.add(new Timestamp(System.currentTimeMillis() - 3 * 60 * 1000));
        timestampList1.add(new Timestamp(System.currentTimeMillis() - 2 * 60 * 1000));

        for (int t = 0; t < timer; t++) {
            pathList2.add(new LatLng(39.925037, 116.423926 + timer * 0.001));
            timestampList2.add(new Timestamp(System.currentTimeMillis() - 2 * 60 * 1000 + timer * 1000));
        }

        sectionList1.add(new RouteSectionInfo(1, pathList1));
        sectionList1.add(new RouteSectionInfo(2, pathList2));
        routeInfoList.add(new RouteInfo(101, sectionList1));


        pathList3.add(new LatLng(39.914568, 116.424804));
        pathList3.add(new LatLng(39.914568, 116.423484));
        timestampList3.add(new Timestamp(System.currentTimeMillis() - 3 * 60 * 1000));
        timestampList3.add(new Timestamp(System.currentTimeMillis() - 2 * 60 * 1000));
        for (int t = 0; t < timer; t++) {
            pathList3.add(new LatLng(39.914568, 116.423484 + timer * 0.001));
            timestampList3.add(new Timestamp(System.currentTimeMillis() - 2 * 60 * 1000 + timer * 1000));
        }

        sectionList2.add(new RouteSectionInfo(3, pathList3, timestampList3));
        routeInfoList.add(new RouteInfo(102, sectionList2));

        timer++;
        return routeInfoList;
    }
}


class NoRepeatRandom {
    private static Set<Integer> integerSet = new HashSet<>();
    private static Random random = new Random();

    static int gen() {
        int num = random.nextInt();
        while (integerSet.contains(num)) {
            num = random.nextInt();
        }
        integerSet.add(num);
        return num;
    }
}

// 保存自身位置和路径
class RouteInfo {
    int id;
    List<RouteSectionInfo> routeSectionInfoList;

    public RouteInfo(int id, List<RouteSectionInfo> routeSectionInfoList) {
        this.id = id;
        this.routeSectionInfoList = routeSectionInfoList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<RouteSectionInfo> getRouteSectionInfoList() {
        return routeSectionInfoList;
    }

    public void setRouteSectionInfoList(List<RouteSectionInfo> routeSectionInfoList) {
        this.routeSectionInfoList = routeSectionInfoList;
    }
}

// 一条Route由不同的Section组成。每个Sectino对应一个司机。一个司机可以对应多个Section。
class RouteSectionInfo {
    int section_id;
    List<LatLng> path;
    List<Timestamp> timestampList;  // optional

    public RouteSectionInfo(int section_id, List<LatLng> path) {
        this.section_id = section_id;
        this.path = path;
    }

    public RouteSectionInfo(int section_id, List<LatLng> path, List<Timestamp> timestampList) {
        this(section_id, path);
        this.timestampList = timestampList;
    }

    public List<LatLng> getPath() {
        return path;
    }

    public void setPath(List<LatLng> path) {
        this.path = path;
    }

    public List<Timestamp> getTimestampList() {
        return timestampList;
    }

    public void setTimestampList(List<Timestamp> timestampList) {
        this.timestampList = timestampList;
    }
}

// 负责使用从WebServices获得的数据来更新本地缓存的RouteList
class RouteListManager {
    private RouteBuilder builder;
    private SparseArray<Route> routeMap;

    RouteListManager(List<RouteInfo> routeInfoList, RouteBuilder builder) {
        this.routeMap = new SparseArray<>();
        this.builder = builder;

        for (RouteInfo routeInfo : routeInfoList) {
            Route route = builder.getInstance(routeInfo);
            routeMap.put(routeInfo.getId(), route);
        }
    }

    List<Route> getActiveRoutes() {
        List<Route> routeList = new LinkedList<>();
        for (int i = 0; i < routeMap.size(); i++) {
            routeList.add(routeMap.valueAt(i));
        }
        return routeList;
    }

    void updateRouteInfo(List<RouteInfo> routeInfoList) {
        SparseArray<Route> newRouteMap = new SparseArray<>();
        for (RouteInfo routeInfo : routeInfoList) {
            int id = routeInfo.getId();
            Route route = routeMap.get(id);
            if (route != null) {
                route.setRouteInfo(routeInfo);
                newRouteMap.put(id, route);
            } else
                newRouteMap.put(id, builder.getInstance(routeInfo));
        }
        this.routeMap = newRouteMap;
    }
}

// 封装Route类的创建方法
class RouteBuilder {
    private MapSDK mapSDK;
    private Route.Callback callback;

    RouteBuilder(MapSDK mapSDK, Route.Callback callback) {
        this.mapSDK = mapSDK;
        this.callback = callback;
    }

    Route getInstance(RouteInfo routeInfo) {
        return new Route(routeInfo, this.mapSDK, this.callback);
    }
}