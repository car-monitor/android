package android.vic;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;

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
    private final int period = 3000;
    private BaiduMap baiduMap;
    private Callback onCarClick;
    private Callback onNetworkError;    // 无法连接到后台时回调
    private Callback onParseError;      // 后台返回的API不能正常解析时回调，通常是服务端返回的API不符合文档

    MapUpdaterThread(BaiduMap baiduMap) {
        this.baiduMap = baiduMap;
    }

    @Override
    public void run() {
        MapSDK mapSDK = new MapSDK(baiduMap);
        mapSDK.centerView(new LatLng(23.16, 113.23));
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
        while (true) {
            for (Route route : routeListManager.getActiveRoutes()) {
                route.removeSelf();
                route.drawSelf();
            }
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

    interface Callback {
        void handleMessage(Bundle bundle);
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

    void setRouteInfo(RouteInfo routeInfo) {
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
                        List<RouteSectionInfo> secList = Route.this.routeInfo.getRouteSectionInfoList();
                        RouteSectionInfo lastSec = secList.get(secList.size() - 1);
                        bundle.putInt("order_id", Route.this.routeInfo.getId());
                        bundle.putInt("car_id", lastSec.getCar_id());
                        bundle.putInt("driver_id", lastSec.getDriver_id());
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
                .color(color).points(convert(path));
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

        OverlayOptions ooDot = new MarkerOptions().icon(pointImage).position(convert(point)).extraInfo(bundle);
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

    void centerView(LatLng center) {
        MapStatus mMapstatus = new MapStatus.Builder().target(convert(center)).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapstatus);
        baiduMap.setMapStatus(mapStatusUpdate);
    }

    private com.baidu.mapapi.model.LatLng convert(LatLng point) {
        return new com.baidu.mapapi.model.LatLng(point.latitude, point.longitude);
    }

    private List<com.baidu.mapapi.model.LatLng> convert(List<LatLng> points) {
        List<com.baidu.mapapi.model.LatLng> newList = new LinkedList<>();
        for (int i = 0; i < points.size(); i++) {
            LatLng p = points.get(i);
            newList.add(convert(p));
        }
        return newList;
    }

    interface Callback {
        void handleMessage(Bundle bundle);
    }
}

// 测试用
class RouteDBDelegateStub implements RouteDBDelegate {
    private int timer = 0;

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

        pathList1.add(new LatLng(23.116326, 113.190565));
        pathList1.add(new LatLng(23.116293, 113.192923));
        pathList2.add(new LatLng(23.118641, 113.192892));
        pathList2.add(new LatLng(23.118553, 113.19437));
        timestampList1.add(new Timestamp(System.currentTimeMillis() - 5 * 60 * 1000));
        timestampList1.add(new Timestamp(System.currentTimeMillis() - 4 * 60 * 1000));
        timestampList1.add(new Timestamp(System.currentTimeMillis() - 3 * 60 * 1000));
        timestampList1.add(new Timestamp(System.currentTimeMillis() - 2 * 60 * 1000));

        for (int t = 0; t < timer; t++) {
            pathList2.add(new LatLng(23.118553, 113.19437 + timer * 0.001));
            timestampList2.add(new Timestamp(System.currentTimeMillis() - 2 * 60 * 1000 + timer * 1000));
        }

        sectionList1.add(new RouteSectionInfo(1, pathList1, timestampList1));
        sectionList1.add(new RouteSectionInfo(2, pathList2, timestampList2));
        sectionList1.get(0).setCar_id(200);
        sectionList1.get(1).setCar_id(300);
        sectionList1.get(0).setDriver_id(2000);
        sectionList1.get(1).setDriver_id(3000);
        routeInfoList.add(new RouteInfo(101, sectionList1));


        pathList3.add(new LatLng(23.146831, 113.321781));
        pathList3.add(new LatLng(23.146873, 113.323771));
        timestampList3.add(new Timestamp(System.currentTimeMillis() - 3 * 60 * 1000));
        timestampList3.add(new Timestamp(System.currentTimeMillis() - 2 * 60 * 1000));
        for (int t = 0; t < timer; t++) {
            pathList3.add(new LatLng(23.146873, 113.323771 + timer * 0.001));
            timestampList3.add(new Timestamp(System.currentTimeMillis() - 2 * 60 * 1000 + timer * 1000));
        }

        sectionList2.add(new RouteSectionInfo(3, pathList3, timestampList3));
        sectionList2.get(0).setCar_id(400);
        sectionList2.get(0).setDriver_id(4000);
        routeInfoList.add(new RouteInfo(102, sectionList2));

        timer++;
        return routeInfoList;
    }
}

class RouteDBDelegateImpl implements RouteDBDelegate {
    static private final String hostname = "localhost";
    static private final String GET_ALL_ORDERS = "getorders";
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public List<RouteInfo> getAllRouteInfo() throws IOException, ParseException {
        Response rawResponse = getDataFromServer();
        GetOrderResponse rsp = parseJSON(rawResponse.body().string());

        if (rsp.status == 0)
            throw new IOException("获取运单数据失败");

        List<RouteInfo> routeInfos = new LinkedList<>();
        for (OrderDetail order : rsp.orderdetails) {
            int id = order.order.id;
            List<LocRecord> route = order.route;
            List<LatLng> path = new ArrayList<>();

            for (int i = 0; i < route.size(); i++) {
                LocRecord loc = route.get(i);
                CoordinateConverter.CoordinateType type = null;
                switch (loc.coordinateType) {
                    case "BD-09":
                        type = CoordinateConverter.CoordinateType.BD_09;
                        break;
                    case "GCJ-05":
                        type = CoordinateConverter.CoordinateType.GCJ_05;
                        break;
                    case "WGS":
                        type = CoordinateConverter.CoordinateType.WGS;
                        break;
                    default:
                        throw new ParseException("Unknown Coordinate Type", 0);
                }

                path.add(CoordinateConverter.convert(
                        new LatLng(route.get(i).latitude, route.get(i).longitude),
                        type));
            }

            List<RouteSectionInfo> sectionInfos = new LinkedList<>();
            RouteSectionInfo sec = new RouteSectionInfo(NoRepeatRandom.gen(), path);
            sec.setCar_id(order.order.carID);
            sec.setDriver_id(order.order.driverId);
            sectionInfos.add(sec);
            routeInfos.add(new RouteInfo(id, sectionInfos));
        }

        return routeInfos;
    }

    private Response getDataFromServer() throws IOException {
        Request request = new Request.Builder().get()
                .url(String.format("http://%s/%s", hostname, GET_ALL_ORDERS))
                .build();
        return client.newCall(request).execute();
    }

    private GetOrderResponse parseJSON(String json) throws ParseException {
        return JSON.parseObject(json, GetOrderResponse.class);
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

class CoordinateConverter {
    static LatLng convert(LatLng point, CoordinateType from) {
        com.baidu.mapapi.utils.CoordinateConverter converter = new com.baidu.mapapi.utils.CoordinateConverter();
        if (from.equals(CoordinateType.GCJ_05))
            converter.from(com.baidu.mapapi.utils.CoordinateConverter.CoordType.COMMON);
        else if (from.equals(CoordinateType.WGS))
            converter.from(com.baidu.mapapi.utils.CoordinateConverter.CoordType.GPS);
        else
            return point;

        converter.coord(new com.baidu.mapapi.model.LatLng(point.latitude, point.longitude));
        com.baidu.mapapi.model.LatLng ll = converter.convert();
        return new LatLng(ll.longitude, ll.latitude);
    }

    enum CoordinateType {BD_09, GCJ_05, WGS}
}

// 保存自身位置和路径
class RouteInfo {
    private int id;
    private List<RouteSectionInfo> routeSectionInfoList;

    RouteInfo(int id, List<RouteSectionInfo> routeSectionInfoList) {
        this.id = id;
        this.routeSectionInfoList = routeSectionInfoList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    List<RouteSectionInfo> getRouteSectionInfoList() {
        return routeSectionInfoList;
    }

    public void setRouteSectionInfoList(List<RouteSectionInfo> routeSectionInfoList) {
        this.routeSectionInfoList = routeSectionInfoList;
    }
}

// 一条Route由不同的Section组成。每个Sectino对应一个司机。一个司机可以对应多个Section。
class RouteSectionInfo {
    private int section_id;
    private int driver_id;
    private int car_id;
    private List<LatLng> path;
    private List<Timestamp> timestampList;  // optional

    RouteSectionInfo(int section_id, List<LatLng> path) {
        this.section_id = section_id;
        this.path = path;
    }

    RouteSectionInfo(int section_id, List<LatLng> path, List<Timestamp> timestampList) {
        this(section_id, path);
        this.timestampList = timestampList;
    }

    public int getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(int driver_id) {
        this.driver_id = driver_id;
    }

    public int getCar_id() {
        return car_id;
    }

    public void setCar_id(int car_id) {
        this.car_id = car_id;
    }

    List<LatLng> getPath() {
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

class GetOrderResponse {
    int status;
    List<OrderDetail> orderdetails;
}

class OrderDetail {
    Order order;
    List<LocRecord> route;
}

class Order {
    int id;
    int carID;
    int driverId;
    String startSite;
    String coordinateType;
    String locationType;
    double startLongitude;
    double startLatitude;
    double endLongitude;
    double endLatitude;
    int isFinished;
    String startTime;
    String endTime;
    String addressorName;
    String addressorPhone;
    String addressorAddress;
    String addresseeName;
    String addresseePhone;
    String addresseeAddress;
    String sealExpect;
    String sealCurrent;
}

class LocRecord {
    int id;
    int carID;
    int waybillID;
    String time;
    String coordinateType;
    String locationType;
    double longitude;
    double latitude;
    int driverId;
    String photoURL;
}

class LatLng {
    double longitude;
    double latitude;

    LatLng(double latitude, double longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
}