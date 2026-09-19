# Assignment 5 — CampusEats HTTP Methods and Headers

A Spring Boot REST service exposing an Order resource, built to demonstrate correct
use of HTTP methods, status codes, conditional requests, idempotency and caching headers.

## Features

- `GET` — list orders, fetch a single order
- `POST` — create an order, cancel an order (`/orders/{id}/cancellation`)
- `PUT` — update an order, guarded by `If-Match`
- `DELETE` — remove an order (204 No Content)
- `OPTIONS` — advertises allowed methods per resource
- `Authorization` header — Bearer token required on every endpoint (401 otherwise)
- `ETag` / `If-None-Match` — conditional GET, 304 Not Modified
- `If-Match` — optimistic concurrency control, 412 Precondition Failed
- `Cache-Control` — `max-age=60` on single-order reads
- `Idempotency-Key` — duplicate POSTs return the existing resource instead of creating another
- Rate limit headers — `X-RateLimit-Limit`, `X-RateLimit-Remaining`
- CORS — `Vary: Origin` and preflight handling

## Endpoints

| Method | Path | Success | Notes |
|---|---|---|---|
| GET | `/orders` | 200 | Collection |
| POST | `/orders` | 201 / 200 | 200 on idempotent replay; `Location` header on create |
| GET | `/orders/{id}` | 200 / 304 | `ETag`, `Cache-Control` |
| PUT | `/orders/{id}` | 200 / 412 | Requires `If-Match` |
| POST | `/orders/{id}/cancellation` | 200 | Status → `CANCELLED` |
| DELETE | `/orders/{id}` | 204 | Empty body |
| any | missing resource | 404 | Problem-detail body |
| any | no `Authorization` | 401 | |

## Run

```bash
mvn spring-boot:run
```

Service starts on `http://localhost:8080`.

## Build

```bash
mvn clean package
```

## Test

All curl invocations and their raw responses are in `curl-transcript.txt`.
A formatted walkthrough of every test is in `TestReport.pdf`.
Design decisions and known gaps are in `NOTES.md`.
The contract is described in `openapi.yaml`.