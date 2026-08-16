## Request 1
Command :curl -i http://localhost:3009/students/1
HTTP/1.1 200 OK
X-Powered-By: tinyhttp
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, HEAD, PUT, PATCH, POST, DELETE
Access-Control-Allow-Headers: content-type
Content-Type: application/json
Date: Sun, 16 Aug 2026 04:11:51 GMT
Connection: keep-alive
Keep-Alive: timeout=5
Content-Length: 60

{
  "id": "1",
  "name": "Nandan Kabra",
  "course": "MCA"
}%

## Request 2
Command :curl -i http://localhost:3009/restaurants/1
HTTP/1.1 200 OK
X-Powered-By: tinyhttp
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, HEAD, PUT, PATCH, POST, DELETE
Access-Control-Allow-Headers: content-type
Content-Type: application/json
Date: Sun, 16 Aug 2026 04:13:46 GMT
Connection: keep-alive
Keep-Alive: timeout=5
Content-Length: 40

{
  "id": "1",
  "name": "Campus Cafe"
}%

## Request 3
Command :url -i http://localhost:3009/students/3
HTTP/1.1 200 OK
X-Powered-By: tinyhttp
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, HEAD, PUT, PATCH, POST, DELETE
Access-Control-Allow-Headers: content-type
Content-Type: application/json
Date: Sun, 16 Aug 2026 04:14:09 GMT
Connection: keep-alive
Keep-Alive: timeout=5
Content-Length: 53

{
  "id": "3",
  "name": "Priya",
  "course": "MCA"
}%

## Request 4
Command :curl -i http://localhost:3009/restaurants/2
HTTP/1.1 200 OK
X-Powered-By: tinyhttp
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, HEAD, PUT, PATCH, POST, DELETE
Access-Control-Allow-Headers: content-type
Content-Type: application/json
Date: Sun, 16 Aug 2026 04:14:34 GMT
Connection: keep-alive
Keep-Alive: timeout=5
Content-Length: 40

{
  "id": "2",
  "name": "Food Corner"
}%

## Request 5
Command :curl -i http://localhost:3009/students/505
HTTP/1.1 404 Not Found
X-Powered-By: tinyhttp
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, HEAD, PUT, PATCH, POST, DELETE
Access-Control-Allow-Headers: content-type
Content-Type: application/json
Date: Sun, 16 Aug 2026 04:14:55 GMT
Connection: keep-alive
Keep-Alive: timeout=5
Content-Length: 26

{
  "error": "Not Found"
}%
