package android.vic.MapManager;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yangzy on 2017/7/2.
 */
//class RouteDBDelegateImpl implements RouteDBDelegate {
//    static private final String hostname = "localhost";
//    static private final String GET_ALL_ORDERS = "getorders";
//    private final OkHttpClient client = new OkHttpClient();
//
//    @Override
//    public List<RouteInfo> getAllRouteInfo() throws IOException, ParseException {
//        Response rawResponse = getDataFromServer();
//        GetOrderResponse rsp = parseJSON(rawResponse.body().string());
//
//        if (rsp.status == 0)
//            throw new IOException("获取运单数据失败");
//
//        List<RouteInfo> routeInfos = new LinkedList<>();
//        for (OrderDetail order : rsp.orderdetails) {
//            int id = order.order.id;
//            List<LocRecord> route = order.route;
//            List<LatLng> path = new ArrayList<>();
//
//            for (int i = 0; i < route.size(); i++) {
//                LocRecord loc = route.get(i);
//                CoordinateConverter.CoordinateType type = null;
//                switch (loc.coordinateType) {
//                    case "BD-09":
//                        type = CoordinateConverter.CoordinateType.BD_09;
//                        break;
//                    case "GCJ-05":
//                        type = CoordinateConverter.CoordinateType.GCJ_05;
//                        break;
//                    case "WGS":
//                        type = CoordinateConverter.CoordinateType.WGS;
//                        break;
//                    default:
//                        throw new ParseException("Unknown Coordinate Type", 0);
//                }
//
//                path.add(CoordinateConverter.convert(
//                        new LatLng(route.get(i).latitude, route.get(i).longitude),
//                        type));
//            }
//
//            List<RouteSectionInfo> sectionInfos = new LinkedList<>();
//            RouteSectionInfo sec = new RouteSectionInfo(NoRepeatRandom.gen(), path);
//            sec.setCar_id(order.order.carID);
//            sec.setDriver_id(order.order.driverId);
//            sectionInfos.add(sec);
//            routeInfos.add(new RouteInfo(id, sectionInfos));
//        }
//
//        return routeInfos;
//    }
//
//    private Response getDataFromServer() throws IOException {
//        Request request = new Request.Builder().get()
//                .url(String.format("http://%s/%s", hostname, GET_ALL_ORDERS))
//                .build();
//        return client.newCall(request).execute();
//    }
//
//    private GetOrderResponse parseJSON(String json) throws ParseException {
//        return JSON.parseObject(json, GetOrderResponse.class);
//    }
//}
