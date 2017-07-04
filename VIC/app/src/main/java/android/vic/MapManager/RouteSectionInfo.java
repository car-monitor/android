package android.vic.MapManager;

import java.util.Date;
import java.util.List;

/**
 * Created by yangzy on 2017/7/2.
 */ // 一条Route由不同的Section组成。每个Sectino对应一个司机。一个司机可以对应多个Section。
class RouteSectionInfo {
    private int section_id;
    private int driver_id;
    private int car_id;
    private List<LatLng> path;
    private List<Date> timestampList;  // optional

    RouteSectionInfo(int section_id, List<LatLng> path) {
        this.section_id = section_id;
        this.path = path;
    }

    RouteSectionInfo(int section_id, List<LatLng> path, List<Date> timestampList) {
        this(section_id, path);
        this.timestampList = timestampList;
    }

    public int getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(int driver_id) {
        this.driver_id = driver_id;
    }

    public int getCar_id() {
        return car_id;
    }

    public void setCar_id(int car_id) {
        this.car_id = car_id;
    }

    List<LatLng> getPath() {
        return path;
    }

    public void setPath(List<LatLng> path) {
        this.path = path;
    }

    public List<Date> getTimestampList() {
        return timestampList;
    }

    public void setTimestampList(List<Date> timestampList) {
        this.timestampList = timestampList;
    }
}
