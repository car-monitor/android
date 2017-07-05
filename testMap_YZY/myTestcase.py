# coding=utf-8
import json
import time
import web  # dependency: web.py
from Utils import *

urls = (
    '/(.*)', 'CarMonitorServicesStub'
)
app = web.application(urls, globals())

if __name__ == "__main__":
    app.run()


# 本样例是我自己撰写的数据，车辆会移动

class CarMonitorServicesStub:
    def __init__(self):
        pass

    count = 0

    def GET(self, name):
        order1 = gen_order(id=100)
        order1["route"].append(gen_point(23.116326, 113.190565, time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time() - 5000))))
        order1["route"].append(gen_point(23.116293, 113.192923, time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time() - 4000))))
        order1["route"].append(gen_point(23.118641, 113.192892, time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time() - 3000))))
        order1["route"].append(gen_point(23.118553, 113.19437, time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time() - 2000))))

        order2 = gen_order(id=200)
        order2["route"].append(gen_point(23.146831, 113.321781, time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time() - 3000))))
        order2["route"].append(gen_point(23.146873, 113.323771, time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time() - 2000))))

        for i in range(CarMonitorServicesStub.count):
            order1["route"].append(gen_point(23.118553, 113.19437 + CarMonitorServicesStub.count * 0.01, time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())))
            order2["route"].append(gen_point(23.146873, 113.323771 + CarMonitorServicesStub.count * 0.01, time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())))

        CarMonitorServicesStub.count += 1
        return json.dumps({"status": 1, "orderdetails": [order1, order2]})
