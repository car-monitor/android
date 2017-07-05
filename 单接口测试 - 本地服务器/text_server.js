// JS是单线程，一次只能处理一个请求，如果中间有别的请求，返回值会出现问题

function getIPAdress(){
    var interfaces = require('os').networkInterfaces();

    for(var devName in interfaces) {
        var iface = interfaces[devName];

        for(var i=0;i<iface.length;i++) {
            var alias = iface[i];

            if(alias.family === 'IPv4' && alias.address !== '127.0.0.1' && !alias.internal){
                return alias.address;
            }
        }
    }
}

http        = require('http');
url         = require('url');
querystring = require('querystring');
rf          = require("fs");

const hostname = getIPAdress();
const port = 8888;
i = 1;

const server = http.createServer((req, res) => {
    console.log("--------------------------------------------");
    console.log("method: " + req.method);
    console.log("url: " + req.url);

    // 判断请求方法和URL
    if (req.method == "POST" && req.url == "/login") {
        // 读取数据
        postData = '';
        req.on('data', function(data) {
            postData += data;
        });

        // 读取完成后的回调
        req.on('end', function() {
            console.log(querystring.parse(postData));
            res.statusCode = 200;
            res.setHeader('cookie', "sessionID");
            res.end("{\"status\":1,\"user\": {\"id\": 666, \"authority\": 0, \"sex\": 0, \"driverType\": \"C1\", \"identify\": \"123\", \"phone\": \"phone\", \"photoURL\": \"photoURL\", \"address\": \"add\", \"companyID\": 4, \"appartmentID\": 78, \"jobNo\": 54}}");
        });
    }

    else if (req.method == "GET") {
        i += 1;
        console.log("i: " + i);
        res.statusCode = 200;
        var params = url.parse(req.url, true).query;
        console.log(params);
        res.statusCode = 200;

        var cardata=rf.readFileSync("car.json","utf-8");
        var acar = rf.readFileSync("acar.json", "utf-8");

        if (req.url.split("?")[0] == '/getcars')
            res.end(cardata);

        if (req.url.split("?")[0] == '/getcar') {
            res.end(acar);
        }

        if (req.url.split("?")[0] == '/getorders') {
            res.end(rf.readFileSync("order.json", "utf-8"));
        }

        if (req.url.split("?")[0] == '/getorder') {
            res.end(rf.readFileSync("aorder.json", "utf-8"));
        }

        if (req.url.split("?")[0] == '/getusers') {
            res.end(rf.readFileSync("driver.json", "utf-8"));
        }

        if (req.url.split("?")[0] == '/getuser') {
            res.end(rf.readFileSync("auser.json", "utf-8"));
        }






        // if (req.url.split("?")[0] == '/getmessage')
        //     res.end('{\"status\": 0, \"infos\": [{\"type\": 0, \"sender\": {\"id\":' + i + ', \"username\": \"name\",  \"authority\":2,\"sex\":0,\"driverType\": \"C1\", \"identify\": \"idetty\", \"phone\": \"10086\", \"photoURL\": \"urls\", \"address\": \"addrs\", \"companyID\": \"con\", \"appartmentID\": \"apart\", \"jobNo\": 12123}, \"title\": \"title\", \"content\": \"contents();\"}]}')

        // if (req.url.split("?")[0] == "/getunit")
        //    res.end("{\"status\":1,\"unit\": {\"id\": 2, \"name\": \"UNIT NAME\"}}");
        // else
        //    res.end("{\"status\":1,\"department\": {\"id\": 78, \"unitid\": 2, \"name\": \"UNIT NAME\"}}");
    }
    console.log("--------------------------------------------");
});

server.listen(port, hostname, () => {
    console.log(`服务器运行在 http://${hostname}:8888/`);
});
