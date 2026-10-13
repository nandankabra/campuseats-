# CampusEats — Assignment 6: Honest Responses, Status Codes, Errors & Validation

**CS 543 · Web Services · Assignment 6 of 12**  
**Team / Group:** Group 8  

### Team Members & Roll Numbers
| Name | Roll Number | Role |
| :--- | :---: | :--- |
| **Priya** | 20251651073 | Team Leader |
| **Nandan Kabra** | 20251651060 | Member |
| **Neeraj Sharma** | 20251651063 | Member |
| **Abhinav Gupta** | 20251651002 | Member |

---

## Answers to Assignment Questions

### 1. Your status map: every endpoint, its success status, and the status for each failure — one line on why each.

* **`POST /orders`**
  * `201 Created`: Returned when a new order resource is successfully created and persisted, with a `Location: /orders/{id}` header pointing to it.
  * `200 OK`: Returned when an idempotent replay is detected via a repeated `Idempotency-Key`, safely returning the previously created order without duplicating it.
  * `401 Unauthorized`: Returned when the request lacks a valid `Authorization: Bearer <token>` header.
  * `409 Conflict`: Returned when an item in the order is unavailable or out of stock (`item-unavailable`).
  * `422 Unprocessable Entity`: Returned when request validation fails (e.g., negative/zero qty, missing address, empty cart) or payment is declined (`payment-declined`).
  * `429 Too Many Requests`: Returned when the rate limit threshold (100 req) is exceeded, accompanied by `Retry-After: 60`.
  * `500 Internal Server Error`: Returned when an unexpected storage or infrastructure exception occurs, masked cleanly without internal leakage.

* **`GET /orders`**
  * `200 OK`: Returned on successful retrieval of the active orders collection.
  * `401 Unauthorized`: Returned when authentication credentials are missing or invalid.
  * `429 Too Many Requests`: Returned when client request rate exceeds permitted quota.

* **`GET /orders/{id}`**
  * `200 OK`: Returned when the requested order exists, with deterministic `ETag` and `Cache-Control: max-age=60` headers.
  * `304 Not Modified`: Returned on conditional request when `If-None-Match` matches the current resource `ETag`, saving bandwidth.
  * `401 Unauthorized`: Returned when authentication token is missing or malformed.
  * `404 Not Found`: Returned when the requested order ID does not exist in the store (`order-not-found`).
  * `406 Not Acceptable`: Returned when the requested format in the `Accept` header (e.g. `text/csv`) cannot be produced by the server.

* **`PATCH /orders/{id}`**
  * `200 OK`: Returned when the order status or delivery address is successfully partially updated.
  * `401 Unauthorized`: Returned when authentication header is absent or invalid.
  * `404 Not Found`: Returned when attempting to patch an order ID that does not exist (`order-not-found`).
  * `409 Conflict`: Returned when the target status violates state transition rules (e.g. modifying an already `CANCELLED` order) (`illegal-transition`).
  * `422 Unprocessable Entity`: Returned when patch fields contain invalid data.

* **`DELETE /orders/{id}`**
  * `204 No Content`: Returned when the order is successfully deleted, with an empty response body.
  * `401 Unauthorized`: Returned when authorization credentials are not provided.
  * `404 Not Found`: Returned when attempting to delete an order that does not exist or has already been deleted (`order-not-found`).

* **`OPTIONS /orders`**
  * `204 No Content`: Returned for preflight discovery, listing allowed HTTP methods (`Allow: GET,POST,PATCH,DELETE,OPTIONS`).

---

### 2. Your type catalogue: the stable string for each failure and what triggers it.

All errors return RFC 7807 Problem Details (`application/problem+json; charset=utf-8`) with stable `type` strings:

1. **`empty-cart`**
   * *Trigger:* Triggered on `POST /orders` when the `items` array is provided but empty (`[]`), meaning no food items were placed in the basket.
   * *HTTP Status:* `422 Unprocessable Entity`

2. **`item-unavailable`**
   * *Trigger:* Triggered on `POST /orders` when one or more requested items in the cart are out of stock or discontinued (e.g. item name contains `OUT_OF_STOCK` or `UNAVAILABLE`).
   * *HTTP Status:* `409 Conflict`

3. **`payment-declined`**
   * *Trigger:* Triggered on `POST /orders` when the payment method is declined by the gateway due to insufficient funds, an expired card, or invalid payment details.
   * *HTTP Status:* `422 Unprocessable Entity`

4. **`order-not-found`**
   * *Trigger:* Triggered on `GET /orders/{id}`, `PATCH /orders/{id}`, or `DELETE /orders/{id}` when the requested order ID is not present in the system.
   * *HTTP Status:* `404 Not Found`

5. **`illegal-transition`**
   * *Trigger:* Triggered on `PATCH /orders/{id}` when an invalid state transition is requested (for example, attempting to reactivate a `CANCELLED` order back to `CONFIRMED`, or moving `CONFIRMED` back to `CREATED`).
   * *HTTP Status:* `409 Conflict`

6. **`validation-error`**
   * *Trigger:* Triggered on `POST /orders` or `PATCH /orders/{id}` when one or more request body fields fail schema constraints (e.g., blank address, quantity $\le 0$, missing name).
   * *HTTP Status:* `422 Unprocessable Entity`

7. **`rate-limit-exceeded`**
   * *Trigger:* Triggered by `RateLimitFilter` when a client exceeds the limit of 100 requests. Returns `Retry-After: 60`.
   * *HTTP Status:* `429 Too Many Requests`

8. **`internal-error`**
   * *Trigger:* Triggered when an internal database deadlock, network timeout, or unexpected exception occurs during server execution.
   * *HTTP Status:* `500 Internal Server Error`

---

### 3. Paste one Problem Details body from your service and label its fields.

Below is an actual Problem Details JSON payload emitted by the service (`HTTP 409 Conflict`):

```json
{
  "type": "item-unavailable",
  "title": "Item Unavailable",
  "status": 409,
  "detail": "Item 'OUT_OF_STOCK Burger' is currently out of stock or unavailable"
}
```

#### Field Labels:
* **`type`**: `item-unavailable` — A stable URI/slug identifier specifying the error type so API clients can write programmatic branch handling without parsing human language.
* **`title`**: `"Item Unavailable"` — A short, human-readable summary of the problem type that remains constant for all occurrences of this error.
* **`status`**: `409` — The HTTP status code generated by the origin server for this occurrence of the problem, aligning with RFC 7807.
* **`detail`**: `"Item 'OUT_OF_STOCK Burger' is currently out of stock or unavailable"` — A human-readable explanation specific to this particular request occurrence explaining why the item was rejected.
* **`Content-Type` Header**: `application/problem+json; charset=utf-8` — Declares that the payload conforms to the RFC 7807 Problem Details media specification.

---

### 4. Show a field-level 422 that reports two bad fields at once.

When a client submits `POST /orders` with both an empty delivery address and an invalid item quantity ($\le 0$), the server validates all constraints before processing and collects all failures into a single `422 Unprocessable Entity` response:

#### Request:
```bash
curl -i -H "Authorization: Bearer demo" \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Nandan Kabra","address":"","items":[{"name":"Burger","qty":0,"price":100}]}' \
  -X POST http://localhost:8080/orders
```

#### Response:
```http
HTTP/1.1 422 Unprocessable Entity
Content-Type: application/problem+json; charset=utf-8
Transfer-Encoding: chunked

{
  "type": "validation-error",
  "title": "Validation Failed",
  "status": 422,
  "detail": "Request validation failed with 2 error(s)",
  "errors": [
    {
      "field": "items[0].qty",
      "reason": "Quantity must be an integer >= 1"
    },
    {
      "field": "address",
      "reason": "Delivery address is required"
    }
  ]
}
```

The `errors[]` array lists all offending fields (`items[0].qty` and `address`) along with descriptive reasons, enabling client UIs to highlight all invalid input fields simultaneously.

---

### 5. Which endpoints are safe to retry, and which status codes tell a client so?

#### Endpoints Safe to Retry:
1. **`GET /orders` & `GET /orders/{id}`**: Inherently safe and idempotent under RFC 9110. Multiple identical GET calls do not alter server state.
2. **`DELETE /orders/{id}`**: Idempotent under HTTP semantics. Retrying deletion leaves the resource deleted.
3. **`OPTIONS /orders`**: Safe and idempotent discovery request.
4. **`POST /orders` (WITH `Idempotency-Key` header)**: Safe to retry because the server checks the idempotency store; repeat requests return `200 OK` with the existing order rather than creating duplicate orders.
5. **`POST /orders` (WITHOUT `Idempotency-Key`)**: **NOT safe to retry automatically**, as a transient network timeout on response could cause the order to be processed twice and charged multiple times.
6. **`PATCH /orders/{id}`**: Conditional retry only; state transitions (e.g. CANCEL) are idempotent once transitioned, but arbitrary field increments are not.

#### Status Codes That Tell a Client to Retry:
* **`429 Too Many Requests`**: Signals temporary rate throttling. Accompanied by `Retry-After: <seconds>` indicating exactly how long the client must back off before retrying.
* **`503 Service Unavailable`**: Signals temporary service unavailability (e.g., maintenance or transient overload). Accompanied by `Retry-After: <seconds>`.
* **Rules on 4xx Statuses**: Clients must **never retry 4xx errors unchanged** (`400 Bad Request`, `401 Unauthorized`, `404 Not Found`, `409 Conflict`, `422 Unprocessable Entity`), because 4xx represents a client-side problem that will reliably fail again unless the request payload or credentials are corrected.

---

### 6. Where did you map an internal error to a clean client error, and what did you hide from the caller?

#### Implementation Location:
In `OrderService.java` (`createOrder` method) and `GlobalExceptionHandler.java`:
```java
// In OrderService.java:
try {
    // Low-level database transaction execution
    executeDatabaseWrite(order);
} catch (Exception ex) {
    // Server logs the real internal exception with full stack trace & query details
    log.error("Internal storage failure during order creation: {}", ex.getMessage(), ex);
    // Throws a clean domain exception
    throw new InternalServiceException("An unexpected internal error occurred while processing your order. Please try again later.");
}

// In GlobalExceptionHandler.java:
@ExceptionHandler(Exception.class)
public ResponseEntity<Problem> handleGenericException(Exception ex) {
    log.error("Unhandled internal exception caught: {}", ex.getMessage(), ex);
    Problem problem = new Problem(
            "internal-error",
            "Internal Server Error",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected internal error occurred. Please contact system support."
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(PROBLEM_JSON_UTF8)
            .body(problem);
}
```

#### What Was Hidden From the Caller:
* **Hidden:** 
  1. Internal database hostnames, IP addresses, and ports (e.g., `db-shard-01:5432`).
  2. SQL error messages, table and schema names (e.g., `INSERT INTO orders... [FATAL: 57P01 admin shutdown]`).
  3. Java class names, method names, line numbers, and full stack traces.
  4. Authentication or connection pool credentials.
* **Returned to Caller:**
  A clean, sanitized RFC 7807 Problem Details object:
  ```json
  {
    "type": "internal-error",
    "title": "Internal Server Error",
    "status": 500,
    "detail": "An unexpected internal error occurred while processing your order. Please try again later."
  }
  ```

---

### 7. (If done) Show the same order as JSON and as XML, and the Accept header that chose each.

The service implements Content Negotiation using Jackson XML and Spring WebMvc. The representation is chosen strictly based on the client's `Accept` header.

#### 1. Requesting JSON:
* **Header:** `Accept: application/json`
* **Response Header:** `Content-Type: application/json;charset=UTF-8`
* **Response Body:**
```json
{
  "orderId": 4,
  "customerName": "Nandan Kabra",
  "address": "Campus Hostel Block B",
  "items": [
    {
      "name": "Chicken Burger",
      "qty": 2,
      "price": 150
    },
    {
      "name": "French Fries",
      "qty": 1,
      "price": 80
    }
  ],
  "totalAmount": 380,
  "status": "CREATED"
}
```

#### 2. Requesting XML:
* **Header:** `Accept: application/xml`
* **Response Header:** `Content-Type: application/xml;charset=UTF-8`
* **Response Body:**
```xml
<order>
  <orderId>4</orderId>
  <customerName>Nandan Kabra</customerName>
  <address>Campus Hostel Block B</address>
  <items>
    <item>
      <name>Chicken Burger</name>
      <qty>2</qty>
      <price>150</price>
    </item>
    <item>
      <name>French Fries</name>
      <qty>1</qty>
      <price>80</price>
    </item>
  </items>
  <totalAmount>380</totalAmount>
  <status>CREATED</status>
</order>
```

#### 3. Requesting Unsupported Media Type:
* **Header:** `Accept: text/csv`
* **Response Status:** `HTTP/1.1 406 Not Acceptable`
* **Response Body:**
```text
406 Not Acceptable: Supported representations are application/json and application/xml
```
