package android.vic.MapManager;

import java.util.List;

/**
 * Created by yangzy on 2017/7/2.
 */ // 保存自身位置和路径
class RouteInfo {
    private int id;
    private List<RouteSectionInfo> routeSectionInfoList;

    RouteInfo(int id, List<RouteSectionInfo> routeSectionInfoList) {
        this.id = id;
        this.routeSectionInfoList = routeSectionInfoList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    List<RouteSectionInfo> getRouteSectionInfoList() {
        return routeSectionInfoList;
    }

    public void setRouteSectionInfoList(List<RouteSectionInfo> routeSectionInfoList) {
        this.routeSectionInfoList = routeSectionInfoList;
    }
}
