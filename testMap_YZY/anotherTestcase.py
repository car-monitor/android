# coding=utf-8
import json
import web  # dependency: web.py
from Utils import *

urls = (
    '/(.*)', 'CarMonitorServicesStub'
)
app = web.application(urls, globals())

if __name__ == "__main__":
    app.run()


# 本样例使用github上的静态数据

class CarMonitorServicesStub:
    def __init__(self):
        pass

    count = 0

    def GET(self, name):
        orderList = []
        orderList_t = []
        with open("order.json", "r") as file:
            orderList_t = json.loads(file.readline())

        for order_t in orderList_t:
            assert isinstance(order_t, dict)
            order = {"order": {}, "route":[], "carStatuses":[]}
            for key in order_t.keys():
                if key != "routes" or key != "carStatuses":
                    order["order"][key] = order_t[key]
            order["route"] = order_t["routes"]
            order["carStatuses"] = order_t["carStatuses"]
            orderList.append(order)

        return json.dumps({"status":1, "orderdetails":orderList})