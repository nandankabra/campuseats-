# Assignment 1 — Web Services

Coursework for **Web Services (Semester 3, IIIT Vadodara)**. The assignment covers three
parts: designing a service brief for a campus food-ordering system, standing up a mock
REST API for it and logging raw HTTP exchanges, and profiling a live website's network
activity in the browser.

## Contents

| Path | What it is |
| --- | --- |
| [campuseats-api/db.json](campuseats-api/db.json) | Seed data for the mock CampusEats REST API (`students`, `restaurants`) |
| [http-log.md](http-log.md) | Raw request/response transcripts captured with `curl -i` |
| [network-analysis.md](network-analysis.md) | Browser DevTools network profile of https://iiitvadodara.ac.in |
| [brief.md](brief.md) | CampusEats system brief — users, nouns (resources), verbs (actions) |

## Part 1 — CampusEats Brief

`brief.md` describes an online food-ordering platform for a college campus. It
identifies the four user types (students, restaurant/cafeteria staff, delivery personnel,
administrators), the **nouns** that become resources in a REST design (User, Restaurant,
Menu, Food Item, Order, Payment, Delivery, Cart, Review, …), and the **verbs** that become
operations on those resources (Register, Browse, Add to Cart, Place Order, Pay, Track
Order, Update Status, Cancel Order, …).

## Part 2 — Mock API and HTTP Log

`campuseats-api/db.json` backs a [json-server](https://github.com/typicode/json-server)
instance serving two collections:

- `students` — 4 records with `id`, `name`, `course`
- `restaurants` — 2 records with `id`, `name`

### Running it

```bash
cd campuseats-api
npx json-server db.json --port 3009
```

The API is then available at `http://localhost:3009`:

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/students` | List all students |
| GET | `/students/:id` | Fetch one student |
| GET | `/restaurants` | List all restaurants |
| GET | `/restaurants/:id` | Fetch one restaurant |

json-server also exposes `POST`, `PUT`, `PATCH`, and `DELETE` on both collections.

### Capturing requests

Requests were issued with `curl -i` so that the status line and response headers are
included in the output:

```bash
curl -i http://localhost:3009/students/1
curl -i http://localhost:3009/restaurants/1
curl -i http://localhost:3009/students/505   # 404 case
```

`http-log.md` records five such exchanges — four successful `200 OK` lookups and one
`404 Not Found` for a non-existent student ID. Notable headers seen on every response:

- `X-Powered-By: tinyhttp` — the HTTP framework json-server runs on
- `Access-Control-Allow-Origin: *` — CORS is fully open on the mock server
- `Content-Type: application/json`
- `Connection: keep-alive` with `Keep-Alive: timeout=5`

## Part 3 — Network Analysis

`network-analysis.md` profiles a page load of https://iiitvadodara.ac.in using the browser
DevTools Network panel.

| Metric | Value |
| --- | --- |
| Total requests | 112 |
| Data transferred | 29.3 MB |
| Resource size | 30.3 MB |
| Finish time | 27.48 s |
| DOMContentLoaded | 12.63 s |
| Load | 16.48 s |

The slowest single resource was `wow.min.js` (JavaScript) at roughly **9.15 s**. No 3xx
redirects or 4xx client errors appeared among the inspected requests.

## Tools Used

- `curl` — issuing HTTP requests and inspecting raw responses
- `json-server` (via `npx`) — zero-config REST API from a JSON file
- Chrome DevTools Network panel — page load profiling
