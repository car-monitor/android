package android.vic.MapManager;

import android.graphics.Color;
import android.os.Bundle;
import android.vic.R;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yangzy on 2017/7/2.
 */ // 职责：调用地图SDK绘出自己
class Route {
    private static BitmapDescriptor trunkImage = BitmapDescriptorFactory
            .fromResource(R.drawable.truck);
    private static int[] pathColor = {0xAADCAB3D, Color.LTGRAY, Color.YELLOW};
    private RouteInfo routeInfo;
    private MapSDK mapSDK;
    private Callback onCarClicked;

    private Integer path_marker_id = null;
    private Integer car_marker_id = null;

    Route(RouteInfo routeInfo, MapSDK mapSDK, Callback onCarClicked) {
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
