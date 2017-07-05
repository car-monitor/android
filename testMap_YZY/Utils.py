import copy

def gen_order(id):
    order = {"order": {"id": id,
                       "carID": 101,
                       "driverId": 102,
                       "startSite": "startSite",
                       "coordinateType": "BS-09",
                       "locationType": "BD-09",
                       "startLongitude": 123.12,
                       "startLatitude": 23.12,
                       "endLongitude": 125.12,
                       "endLatitude": 25.12,
                       "isFinished": 0,
                       "startTime": "",
                       "endTime": "",
                       "addressorName": "",
                       "addressorPhone": "",
                       "addressorAddress": "",
                       "addresseeName": "",
                       "addresseePhone": "",
                       "addresseeAddress": "",
                       "sealExpect": "",
                       "sealCurrent": ""},
             "route": []}
    return copy.deepcopy(order)


def gen_point(latitude, longitude, time):
    point = {"id": "",
             "carID": "",
             "waybillID": "",
             "time": time,
             "coordinateType": "BS-09",
             "locationType": "",
             "longitude": "",
             "latitude": "",
             "driverId": "",
             "photoURL": ""}

    new_point = copy.deepcopy(point)
    new_point["latitude"] = latitude
    new_point["longitude"] = longitude
    return new_point