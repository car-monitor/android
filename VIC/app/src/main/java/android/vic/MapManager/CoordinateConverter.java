package android.vic.MapManager;

/**
 * Created by yangzy on 2017/7/2.
 */
class CoordinateConverter {
    static LatLng convert(LatLng point, CoordinateType from) {
        com.baidu.mapapi.utils.CoordinateConverter converter = new com.baidu.mapapi.utils.CoordinateConverter();
        if (from.equals(CoordinateType.GCJ_05))
            converter.from(com.baidu.mapapi.utils.CoordinateConverter.CoordType.COMMON);
        else if (from.equals(CoordinateType.WGS))
            converter.from(com.baidu.mapapi.utils.CoordinateConverter.CoordType.GPS);
        else
            return point;

        converter.coord(new com.baidu.mapapi.model.LatLng(point.latitude, point.longitude));
        com.baidu.mapapi.model.LatLng ll = converter.convert();
        return new LatLng(ll.longitude, ll.latitude);
    }

    enum CoordinateType {BD_09, GCJ_05, WGS}
}
