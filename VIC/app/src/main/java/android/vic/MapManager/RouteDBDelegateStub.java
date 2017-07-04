package android.vic.MapManager;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yangzy on 2017/7/2.
 */ // 测试用
class RouteDBDelegateStub implements RouteDBDelegate {
    private int timer = 0;

    public List<RouteInfo> getAllRouteInfo() throws IOException, ParseException {
        List<RouteInfo> routeInfoList = new LinkedList<>();
        List<LatLng> pathList1 = new LinkedList<>();
        List<LatLng> pathList2 = new LinkedList<>();
        List<LatLng> pathList3 = new LinkedList<>();
        List<Date> timestampList1 = new LinkedList<>();
        List<Date> timestampList2 = new LinkedList<>();
        List<Date> timestampList3 = new LinkedList<>();
        List<RouteSectionInfo> sectionList1 = new LinkedList<>();
        List<RouteSectionInfo> sectionList2 = new LinkedList<>();

        pathList1.add(new LatLng(23.116326, 113.190565));
        pathList1.add(new LatLng(23.116293, 113.192923));
        pathList2.add(new LatLng(23.118641, 113.192892));
        pathList2.add(new LatLng(23.118553, 113.19437));
        timestampList1.add(new Date(System.currentTimeMillis() - 5 * 60 * 1000));
        timestampList1.add(new Date(System.currentTimeMillis() - 4 * 60 * 1000));
        timestampList1.add(new Date(System.currentTimeMillis() - 3 * 60 * 1000));
        timestampList1.add(new Date(System.currentTimeMillis() - 2 * 60 * 1000));

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
        timestampList3.add(new Date(System.currentTimeMillis() - 3 * 60 * 1000));
        timestampList3.add(new Date(System.currentTimeMillis() - 2 * 60 * 1000));
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
