package android.vic.MapManager;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yangzy on 2017/7/2.
 */
class RouteDBDelegateImpl implements RouteDBDelegate {
    static private final String HOSTNAME = "localhost";
    static private final String GET_ALL_ORDERS = "getorders";
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public List<RouteInfo> getAllRouteInfo() throws IOException, ParseException {
        Response rawResponse = getDataFromServer();

        String rsp_json = rawResponse.body().string();
        Object rootConfig = Configuration.defaultConfiguration().jsonProvider().parse(rsp_json);
        boolean status = Boolean.parseBoolean((String)JsonPath.read(rootConfig, "$.status"));
        if (!status)
            throw new IOException("return status == 0");

        List<String> orderDetails = JsonPath.read(rootConfig, "$.orderdetails[?(@.order.isFinished == 0)]");
        List<RouteInfo> routeInfos = new LinkedList<>();
        for (String orderDetail : orderDetails) {
            Object orderDetailConfig = Configuration.defaultConfiguration().jsonProvider().parse(orderDetail);

            int id = Integer.parseInt((String)JsonPath.read(orderDetailConfig, "$.order.id"));
            int carID = Integer.parseInt((String)JsonPath.read(orderDetailConfig, "$.order.carID"));
            int driverId = Integer.parseInt((String)JsonPath.read(orderDetailConfig, "$.order.driverId"));

            List<String> route = JsonPath.read(orderDetailConfig, "$.route");
            List<LatLng> points = new ArrayList<>();
            List<Date> timestamps = new LinkedList<>();
            for (String point_json : route) {
                Object pointConfig = Configuration.defaultConfiguration().jsonProvider().parse(point_json);
                String coordinateType = JsonPath.read(pointConfig, "$.coordinateType");
                double latitude = Double.valueOf((String) JsonPath.read(pointConfig, "$.latitude"));
                double longitude = Double.valueOf((String) JsonPath.read(pointConfig, "$.longitude"));
                Date time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse((String) JsonPath.read(pointConfig, "$.time"));
                points.add(CoordinateConverter.convert(new LatLng(latitude, longitude), coordinateType));
                timestamps.add(time);
            }
            List<RouteSectionInfo> routeSectionInfos = new LinkedList<>();
            RouteSectionInfo routeSectionInfo = new RouteSectionInfo(NoRepeatRandom.gen(), points);
            routeSectionInfo.setDriver_id(driverId);
            routeSectionInfo.setCar_id(carID);
            routeSectionInfo.setTimestampList(timestamps);
            routeSectionInfos.add(routeSectionInfo);
            routeInfos.add(new RouteInfo(id, routeSectionInfos));
        }
        return routeInfos;
    }

    private Response getDataFromServer() throws IOException {
        Request request = new Request.Builder().get()
                .url(String.format("http://%s/%s", HOSTNAME, GET_ALL_ORDERS))
                .build();
        return client.newCall(request).execute();
    }
}
