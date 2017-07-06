package android.vic.MapManager;

import android.util.SparseArray;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yangzy on 2017/7/2.
 */ // 负责使用从WebServices获得的数据来更新本地缓存的RouteList
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
