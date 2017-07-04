package android.vic.MapManager;

import android.os.Bundle;
import android.util.SparseArray;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yangzy on 2017/7/2.
 */

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

    LatLng getMapCenter() {
        MapStatus mapStatus = baiduMap.getMapStatus();
        return new LatLng(mapStatus.target.latitude, mapStatus.target.longitude);
    }
}