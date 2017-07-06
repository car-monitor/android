package android.vic.MapManager;

import android.util.Pair;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yangzy on 2017/7/2.
 */
class RouteDBDelegateImpl implements RouteDBDelegate {
    static private final String HOSTNAME = "10.0.2.2:1304";
    static private final String GET_ALL_ORDERS = "getorders";
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public List<RouteInfo> getAllRouteInfo() throws IOException, ParseException {
        Response rawResponse = getDataFromServer();
        List<RouteInfo> routeInfos_unsort = parseJSON(rawResponse.body().string());
        if (routeInfos_unsort == null)
            throw new IOException("Response status:0");

        return routeInfos_unsort;
    }

    private Response getDataFromServer() throws IOException {
        Request request = new Request.Builder().get()
                .url(String.format("http://%s/%s", HOSTNAME, GET_ALL_ORDERS))
                .build();
        return client.newCall(request).execute();
    }

    private List<RouteInfo> parseJSON(String json) throws ParseException {
        Object parserConfig = Configuration.defaultConfiguration().jsonProvider().parse(json);
        if ((Integer)JsonPath.read(parserConfig, "$.status") == 0)
            return null;

        List<Map> orderDetails = JsonPath.read(parserConfig, "$.orderdetails[?(@.order.isFinished == 0)]");
        List<RouteInfo> routeInfos = new LinkedList<>();
        for (Map orderDetail : orderDetails) {
            Map order = (Map)orderDetail.get("order");
            int id = (Integer)order.get("id");
            int carID = (Integer)order.get("carID");
            int driverId = (Integer)order.get("driverId");

            List<Map> route = (List<Map>) orderDetail.get("route");
            List<LatLng> points = new ArrayList<>();
            List<Date> timestamps = new LinkedList<>();
            for (Map point : route) {
                String coordinateType = (String) point.get("coordinateType");
                double latitude = (double) point.get("latitude");
                double longitude = (double) point.get("longitude");
                Date time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse((String) point.get("time"));
                points.add(CoordinateConverter.convert(new LatLng(latitude, longitude), coordinateType));
                timestamps.add(time);
            }
            List<RouteSectionInfo> routeSectionInfos = new LinkedList<>();
            sort_path(points, timestamps);
            RouteSectionInfo routeSectionInfo = new RouteSectionInfo(NoRepeatRandom.gen(), points);
            routeSectionInfo.setDriver_id(driverId);
            routeSectionInfo.setCar_id(carID);
            routeSectionInfo.setTimestampList(timestamps);
            routeSectionInfos.add(routeSectionInfo);
            routeInfos.add(new RouteInfo(id, routeSectionInfos));
        }
        return routeInfos;
    }

    private void sort_path(List<LatLng> points, List<Date> timestamps) {
        List<Pair<LatLng, Date>> pairs = new LinkedList<>();
        for (int i = 0; i < points.size(); i++)
            pairs.add(new Pair<LatLng, Date>(points.get(i), timestamps.get(i)));
        Collections.sort(pairs, new Comparator<Pair<LatLng, Date>>() {
            @Override
            public int compare(Pair<LatLng, Date> t1, Pair<LatLng, Date> t2) {
                boolean result = t1.second.after(t2.second);
                if (result) {
                    return 1;
                }
                else if (!result) {
                    return -1;
                }
                else {
                    return 0;
                }
            }
        });
        points.clear();
        timestamps.clear();
        for (int i = 0; i < pairs.size(); i++) {
            points.add(pairs.get(i).first);
            timestamps.add(pairs.get(i).second);
        }
    }
}
