package android.vic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;


/**
 * 地图车辆数据的远程获取 & 异步加载
 * Created by yangzy on 2017/6/30.
 */

// 顶层模块，调用各模块实现地图更新
class MapUpdaterThread extends Thread {
    private final int period = 5000;
    private BaiduMap baiduMap;
    private Activity activity;

    MapUpdaterThread(BaiduMap baiduMap, Activity activity) {
        this.baiduMap = baiduMap;
        this.activity = activity;
    }

    @Override
    public void run() {
        MapSDK mapSDK = new MapSDK(baiduMap);
        CarBuilder carBuilder = new CarBuilder(mapSDK, new Car.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                Intent intent = new Intent(activity, CarDetailActivity.class);
                intent.putExtras(bundle);
                activity.startActivity(intent);
            }
        });
        RouteDBDelegate routeDBDelegate = new RouteDBDelegate();
        CarListManager carListManager = new CarListManager(routeDBDelegate.getAllCarInfo(), carBuilder);

        // 每3s从后台服务器获得车辆位置数据，并加载到地图上
        while (true) {
            for (Car car : carListManager.getActiveCars()) {
                car.removeSelf();
                car.drawSelf();
            }
            carListManager.updateCarInfo(routeDBDelegate.getAllCarInfo());
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
}

// 职责：调用地图SDK绘出自己
// 保存自身位置和路径的职责被封装到CarInfo类中
class Car {
    private static BitmapDescriptor trunkImage = BitmapDescriptorFactory
            .fromResource(R.drawable.truck);

    private CarInfo carInfo;
    private MapSDK mapSDK;
    private Car.Callback onCarClicked;

    Car(CarInfo carInfo, MapSDK mapSDK, Car.Callback onCarClicked) {
        this.carInfo = carInfo;
        this.mapSDK = mapSDK;
        this.onCarClicked = onCarClicked;
    }

    public CarInfo getCarInfo() {
        return carInfo;
    }

    public void setCarInfo(CarInfo carInfo) {
        this.carInfo = carInfo;
    }

    void drawSelf() {
        assert carInfo.getPath() != null;
        if (BuildConfig.DEBUG && carInfo.getPath().size() <= 0) {
            throw new AssertionError("drawSelf:path.size()<=0");
        }

        LatLng curPoint = carInfo.getPath().get(carInfo.getPath().size() - 1);
        mapSDK.drawPoint(carInfo.getId(), curPoint, trunkImage, new MapSDK.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                bundle.putInt("id", Car.this.carInfo.getId());
                Car.this.onCarClicked.handleMessage(bundle);
            }
        });
        mapSDK.drawPath(carInfo.getId(), carInfo.getPath());
    }

    void removeSelf() {
        mapSDK.removePath(carInfo.getId());
        mapSDK.removePoint(carInfo.getId());
    }

    interface Callback {
        void handleMessage(Bundle bundle);
    }
}

// 封装百度地图绘图方法
class MapSDK {
    private BaiduMap baiduMap;  // 地图

    // Map容器，用于将元素（如车辆点、车辆路径）跟其id建立映射
    private SparseArray<Overlay> pointMap;
    private SparseArray<Overlay> pathMap;
    private SparseArray<Callback> callbackMap;

    MapSDK(BaiduMap baiduMap) {
        this.pointMap = new SparseArray<>();
        this.pathMap = new SparseArray<>();
        this.callbackMap = new SparseArray<>();
        this.baiduMap = baiduMap;
        this.baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                int id = (int) marker.getExtraInfo().get("id");
                callbackMap.get(id).handleMessage(marker.getExtraInfo());
                return true;
            }
        });
    }

    void drawPath(int id, List<LatLng> path) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);

        OverlayOptions ooPolyline = new PolylineOptions().width(20)
                .color(0xAADCAB3D).points(path);
        Overlay overlay = baiduMap.addOverlay(ooPolyline);

        pathMap.put(id, overlay);
    }

    void drawPoint(int id, LatLng point, BitmapDescriptor pointImage, Callback OnClick) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);

        OverlayOptions ooDot = new MarkerOptions().icon(pointImage).position(point).extraInfo(bundle);
        Overlay overlay = baiduMap.addOverlay(ooDot);

        pointMap.put(id, overlay);
        callbackMap.put(id, OnClick);
    }

    boolean removePoint(int id) {
        Overlay overlay = pointMap.get(id);
        Callback callback = callbackMap.get(id);
        if (overlay == null || callback == null)
            return false;

        overlay.remove();
        pointMap.remove(id);
        callbackMap.remove(id);

        return true;
    }

    boolean removePath(int id) {
        Overlay overlay = pathMap.get(id);
        if (overlay == null)
            return false;

        overlay.remove();
        pathMap.remove(id);

        return true;
    }

    interface Callback {
        void handleMessage(Bundle bundle);
    }
}

// 连接远程WebService，获取数据
class RouteDBDelegate {
    private final String hostName;
    int timer = 0;

    RouteDBDelegate() {
        this.hostName = "localhost";
    }

    List<CarInfo> getAllCarInfo() {
        List<CarInfo> carInfoList = new LinkedList<>();
        List<LatLng> pathList1 = new LinkedList<>();
        pathList1.add(new LatLng(39.921634, 116.413039));
        pathList1.add(new LatLng(39.921781, 116.417611));
        pathList1.add(new LatLng(39.924843, 116.417566));
        pathList1.add(new LatLng(39.925037, 116.423926));
        List<Timestamp> timestampList1 = new LinkedList<>();
        timestampList1.add(new Timestamp(System.currentTimeMillis() - 5 * 60 * 1000));
        timestampList1.add(new Timestamp(System.currentTimeMillis() - 4 * 60 * 1000));
        timestampList1.add(new Timestamp(System.currentTimeMillis() - 3 * 60 * 1000));
        timestampList1.add(new Timestamp(System.currentTimeMillis() - 2 * 60 * 1000));
        carInfoList.add(new CarInfo(123, pathList1, timestampList1));

        List<LatLng> pathList2 = new LinkedList<>();
        pathList2.add(new LatLng(39.914568, 116.424804));
        pathList2.add(new LatLng(39.914568, 116.423484));
        List<Timestamp> timestampList2 = new LinkedList<>();
        timestampList2.add(new Timestamp(System.currentTimeMillis() - 3 * 60 * 1000));
        timestampList2.add(new Timestamp(System.currentTimeMillis() - 2 * 60 * 1000));
        carInfoList.add(new CarInfo(456, pathList2, timestampList2));

        for (int t = 0; t < timer; t++) {
            pathList1.add(new LatLng(39.925037, 116.423926 + timer * 0.001));
            pathList2.add(new LatLng(39.914568, 116.423484 + timer * 0.001));
            timestampList1.add(new Timestamp(System.currentTimeMillis() - 2 * 60 * 1000 + timer * 1000));
            timestampList2.add(new Timestamp(System.currentTimeMillis() - 2 * 60 * 1000 + timer * 1000));
        }

        timer++;
        return carInfoList;
    }
}


class NoRepeatRandomNumGenerator {
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
class CarInfo {
    int id;
    List<LatLng> path;
    List<Timestamp> timestampList;  // optional

    public CarInfo(int id, List<LatLng> path) {
        this.id = id;
        this.path = path;
    }

    public CarInfo(int id, List<LatLng> path, List<Timestamp> timestampList) {
        this(id, path);
        this.timestampList = timestampList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

// 负责使用从WebServices获得的数据来更新本地缓存的CarList
class CarListManager {
    private CarBuilder factory;
    private SparseArray<Car> carMap;

    CarListManager(List<CarInfo> carInfoList, CarBuilder factory) {
        this.carMap = new SparseArray<>();
        this.factory = factory;

        for (CarInfo carInfo : carInfoList) {
            Car car = factory.getCarInstance(carInfo);
            carMap.put(carInfo.getId(), car);
        }
    }

    List<Car> getActiveCars() {
        List<Car> carList = new LinkedList<>();
        for (int i = 0; i < carMap.size(); i++) {
            carList.add(carMap.valueAt(i));
        }
        return carList;
    }

    void updateCarInfo(List<CarInfo> carInfoList) {
        SparseArray<Car> newCarMap = new SparseArray<>();
        for (CarInfo carInfo : carInfoList) {
            int id = carInfo.getId();
            Car car = carMap.get(id);
            if (car != null) {
                car.setCarInfo(carInfo);
                newCarMap.put(id, car);
            } else
                newCarMap.put(id, factory.getCarInstance(carInfo));
        }
        this.carMap = newCarMap;
    }
}

// 封装Car类的创建方法
class CarBuilder {
    private MapSDK mapSDK;
    private Car.Callback callback;

    CarBuilder(MapSDK mapSDK, Car.Callback callback) {
        this.mapSDK = mapSDK;
        this.callback = callback;
    }

    Car getCarInstance(CarInfo carInfo) {
        return new Car(carInfo, this.mapSDK, this.callback);
    }
}