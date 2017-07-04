#!/usr/bin/python
#coding=utf8
from BaseHTTPServer import BaseHTTPRequestHandler,HTTPServer
import urllib
import cgi

class text_server(BaseHTTPRequestHandler):

    def do_GET(self):
        print "--------------------------------"
        print "Method: GET"
        print "Path: ", self.path
        print self.path
        path, param = urllib.splitquery(self.path)
        print 'path: ', path
        print 'param: ', param
        print "--------------------------------"

    def do_POST(self):
        print "--------------------------------"
        print "Method: POST"
        print "Path: ", self.path

        ctype, pdict = cgi.parse_header(self.headers.getheader('content-type'))
        print 'ctype: ', ctype
        print 'pdict: ', pdict
        if ctype == 'multipart/form-data':
            postvars = cgi.parse_multipart(self.rfile, pdict)
        elif ctype == 'application/x-www-form-urlencoded':
            length = int(self.headers.getheader('content-length'))
            postvars = cgi.parse_qs(self.rfile.read(length), keep_blank_values=1)
        else:
            postvars = {}

        if self.path == '/login':
            print 'pdata: ', postvars
            self.send_response(200)
            self.send_header('cookie', 'text_session')
            self.end_headers()
            self.wfile.write("{\"status\":1,\"user\": {\"id\": 666, \"authority\": 0, \"sex\": 0, \"driverType\": \"C1\", \"identify\": \"123\", \"phone\": \"phone\", \"photoURL\": \"photoURL\", \"address\": \"add\", \"companyID\": 4, \"appartmentID\": 78, \"jobNo\": 54}}");

        else:
            print self.path, " 意外的请求"
        print "--------------------------------"


PORT_NUMBER = 8888
import socket
myname = socket.getfqdn(socket.gethostname())
myaddr = socket.gethostbyname(myname)

try:
    server = HTTPServer((myaddr, PORT_NUMBER), text_server)
    print 'Started run at ', myaddr, PORT_NUMBER
    server.serve_forever()

except KeyboardInterrupt:
    print '^C received, shutting down the web server'
    server.socket.close()
