package android.vic.MapManager;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Created by yangzy on 2017/7/2.
 */

// 连接远程WebService，获取数据
interface RouteDBDelegate {
    List<RouteInfo> getAllRouteInfo() throws IOException, ParseException;
}
