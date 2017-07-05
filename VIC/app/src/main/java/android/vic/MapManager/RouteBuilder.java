package android.vic.MapManager;

/**
 * Created by yangzy on 2017/7/2.
 */ // 封装Route类的创建方法
class RouteBuilder {
    private MapSDK mapSDK;
    private Route.Callback callback;

    RouteBuilder(MapSDK mapSDK, Route.Callback callback) {
        this.mapSDK = mapSDK;
        this.callback = callback;
    }

    Route getInstance(RouteInfo routeInfo) {
        return new Route(routeInfo, this.mapSDK, this.callback);
    }
}
