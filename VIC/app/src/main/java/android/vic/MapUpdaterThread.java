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
    private BaiduMap baiduMap;
    private Activity activity;
    private List<Car> carList;
    private final int period = 5000;

    MapUpdaterThread(BaiduMap baiduMap, Activity activity) {
        this.baiduMap = baiduMap;
        this.activity = activity;
    }

    @Override
    public void run() {
        MapSDK mapSDK = new MapSDK(baiduMap);

        // 第一次从后台服务器获得车辆位置数据，并加载到地图上
        this.carList = new LinkedList<>();
        List<LatLng> pathList1 = new LinkedList<>();
        pathList1.add(new LatLng(39.921634, 116.413039));
        pathList1.add(new LatLng(39.921781, 116.417611));
        pathList1.add(new LatLng(39.924843, 116.417566));
        pathList1.add(new LatLng(39.925037, 116.423926));
        carList.add(new Car(NoRepeatRandomNumGenerator.gen(), mapSDK, pathList1, new Car.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                activity.startActivity(new Intent(activity, CarDetailActivity.class).putExtras(bundle));
            }
        }));

        List<LatLng> pathList2 = new LinkedList<>();
        pathList2.add(new LatLng(39.914568, 116.424804));
        pathList2.add(new LatLng(39.914568, 116.423484));
        carList.add(new Car(NoRepeatRandomNumGenerator.gen(), mapSDK, pathList2, new Car.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                activity.startActivity(new Intent(activity, CarDetailActivity.class).putExtras(bundle));
            }
        }));

        for (Car car : carList)
            car.drawSelf();

        // 每5s从后台服务器获得车辆位置数据
        while (true) {
            try {
                sleep(period);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (Car car:carList) {
                List<LatLng> path = car.getPath();
                LatLng lastPoint = path.get(path.size() - 1);
                List<LatLng> newPathList = new LinkedList<>();
                newPathList.add(new LatLng(lastPoint.latitude, lastPoint.longitude + 0.01));
                car.updatePathAndDraw(newPathList);
            }
        }
    }

}

// 职责：保存自身位置和路径，调用地图SDK绘出自己
class Car {
    private static BitmapDescriptor trunkImage = BitmapDescriptorFactory
            .fromResource(R.drawable.truck);

    private int id;
    private MapSDK mapSDK;
    private List<LatLng> path; // 第一个点为起点，最后一个点为当前位置
    private Car.Callback onCarClicked;

    interface Callback {
        void handleMessage(Bundle bundle);
    }

    Car(int id, MapSDK mapSDK, List<LatLng> path, Car.Callback onCarClicked) {
        this.id = id;
        this.mapSDK = mapSDK;
        this.path = path;
        this.onCarClicked = onCarClicked;
    }

    List<LatLng> getPath() { return path; }

    void drawSelf() {
        assert path != null;
        if(BuildConfig.DEBUG && path.size() <= 0){
            throw new AssertionError("drawSelf:path.size()<=0");
        }

        LatLng curPoint = path.get(path.size() - 1);
        mapSDK.drawPoint(id, curPoint, trunkImage, new MapSDK.Callback() {
            @Override
            public void handleMessage(Bundle bundle) {
                bundle.putInt("id", Car.this.id);
                Car.this.onCarClicked.handleMessage(bundle);
            }
        });
        mapSDK.drawPath(id, path);
    }

    // 将新增的点保存到path容器中，然后绘制到地图上
    void updatePathAndDraw(List<LatLng> newPath) {
        path.addAll(newPath);
        mapSDK.removePath(id);
        mapSDK.removePoint(id);
        drawSelf();
    }
}

// 封装百度地图绘图方法
class MapSDK {
    private BaiduMap baiduMap;  // 地图

    // Map容器，用于将元素（如车辆点、车辆路径）跟其id建立映射
    private SparseArray<Overlay> pointMap;
    private SparseArray<Overlay> pathMap;
    private SparseArray<Callback> callbackMap;

    interface Callback {
        void handleMessage(Bundle bundle);
    }

    MapSDK(BaiduMap baiduMap) {
        this.pointMap = new SparseArray<>();
        this.pathMap = new SparseArray<>();
        this.callbackMap = new SparseArray<>();
        this.baiduMap = baiduMap;
        this.baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                int id = (int)marker.getExtraInfo().get("id");
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
}


//class RemoteCarTrackService {
//    RemoteCarTrackService() {}
//}


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