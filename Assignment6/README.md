# Assignment 6 — CampusEats: Honest Responses, Status Codes, Errors & Validation

**CS 543 · Web Services · Assignment 6 of 12**  
**Group 8**

A Spring Boot REST service for Order management, built to demonstrate honest HTTP status codes, standard RFC 7807 Problem Details, multi-field request validation, error catalogues, content negotiation (JSON & XML), retry safety, and clean exception masking without leaking server internals.

---

## Features

- **Honest Status Codes**: Every response uses the correct semantic HTTP status code (`201`, `200`, `204`, `304`, `401`, `404`, `406`, `409`, `422`, `429`, `500`). Never returns `200 OK` with `{"ok": false}`.
- **Location on Create**: `POST /orders` returns `201 Created` with a `Location: /orders/{id}` header pointing to the newly created resource.
- **Idempotency Replay**: Repeating `POST /orders` with the same `Idempotency-Key` returns `200 OK` with the existing order, preventing duplicates.
- **RFC 7807 Problem Details**: All error responses return standard Problem Details (`type`, `title`, `status`, `detail`, and optional `errors[]`) with `Content-Type: application/problem+json; charset=utf-8`.
- **Type Catalogue**: Stable machine-readable type slugs:
  - `empty-cart` (422)
  - `item-unavailable` (409)
  - `payment-declined` (422)
  - `order-not-found` (404)
  - `illegal-transition` (409)
  - `validation-error` (422)
  - `rate-limit-exceeded` (429)
  - `internal-error` (500)
- **Multi-Field Aggregated Validation**: Request body validation checks all constraints first and collects all violations into a single 422 response with an `errors[]` array naming each bad field and why.
- **Content Negotiation**: Supports both `application/json` (default) and `application/xml` via Jackson XML; returns `406 Not Acceptable` for unsupported types.
- **Retry Safety**: Rate limit 429 response includes the `Retry-After: 60` header.
- **Internal Exception Masking**: Wraps dangerous internal work in `try/catch`, logs the error and stack trace server-side, and returns a clean, sanitized Problem Details error to the client.

---

## Endpoints

| Method | Path | Success | Real Failure Statuses | Notes |
|:---|:---|:---:|:---:|:---|
| **POST** | `/orders` | `201` / `200` | `401`, `409`, `422`, `429`, `500` | Creates order; `Location` header; `200` on idempotent replay |
| **GET** | `/orders` | `200` | `401`, `429` | Lists all active orders |
| **GET** | `/orders/{id}` | `200` / `304` | `401`, `404`, `406`, `429` | Supports `ETag`, `Cache-Control`, `If-None-Match`, content negotiation |
| **PATCH** | `/orders/{id}` | `200` | `401`, `404`, `409`, `422` | Partially updates status or delivery address; state machine checks |
| **DELETE**| `/orders/{id}` | `204` | `401`, `404` | Removes order; empty response body |
| **OPTIONS**| `/orders` | `204` | — | Returns `Allow: GET,POST,PATCH,DELETE,OPTIONS` |

---

## How to Run & Test

### Run Service
```bash
mvn spring-boot:run
```
The service will start on `http://localhost:8080`.

### Run Automated Tests
```bash
mvn test
```
Executes the comprehensive JUnit 5 / MockMvc test suite in `OrderControllerTest.java` (13/13 passing).

### View cURL Transcript
All curl commands and their full HTTP response headers and bodies are captured in `transcript.txt`.

### Documentation
- Answers to all assignment questions are in `NOTES.md`.
- OpenAPI specification is in `openapi.yaml`.
- Submission zip archive is `CampusEats_Assignment6_Group8.zip`.
