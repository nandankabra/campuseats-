# Notes — Assignment 5

## Status codes

- `201 Created` on a new order, with a `Location` header pointing at `/orders/{id}`.
- `200 OK` on an idempotent replay of `POST /orders`, since no new resource is created.
- `204 No Content` on `DELETE`, with no response body.
- `404 Not Found` for a missing order. This was originally returning `400 Bad Request`;
  the exception handler was corrected so that a missing resource maps to 404, which is
  what the semantics of the method call for.
- `401 Unauthorized` when the `Authorization` header is absent.

### Exception handling caveat

The quickest fix is a handler on `RuntimeException`:

```java
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<Problem> handleNotFound(RuntimeException ex) {
    return ResponseEntity.status(404)
            .body(new Problem("Not Found", 404, ex.getMessage()));
}
```

This works but is too broad — every unexpected runtime failure (a null dereference,
a parse error) would then be reported to the client as 404, masking real bugs as
missing data. The version used here declares a dedicated exception instead:

```java
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Order not found");
    }
}

@ExceptionHandler(OrderNotFoundException.class)
public ResponseEntity<Problem> handleNotFound(OrderNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new Problem("Not Found", 404, ex.getMessage()));
}
```

Anything else falls through to a generic 500 handler, which is the correct outcome.

## Conditional requests

`GET /orders/{id}` computes an `ETag` from the current representation and sets
`Cache-Control: max-age=60`. When the client sends a matching `If-None-Match`, the
server replies `304 Not Modified` with no body.

Observed during testing: two successive `GET /orders/1` calls returned different
ETag values (`"1453530583"` and `"483808708"`) even though the order had not changed.
That means the ETag is being derived from something that varies per response — most
likely the object's default `hashCode()`, or a serialization that includes a
timestamp. An ETag must be stable for an unchanged representation, otherwise
`If-None-Match` can never match and 304 is unreachable. The fix is to compute it
from the persisted fields only, for example a hash of
`orderId + customerName + totalAmount + status + version`, or to use a monotonic
`version` column directly.

## Idempotency

`POST /orders` accepts an `Idempotency-Key` header. The key is stored alongside the
resulting order id; a repeat request carrying a key that has been seen returns the
original order with `200 OK` rather than creating a duplicate. This protects against
client retries and double submissions.

The key is currently held in an in-memory map, so it does not survive a restart and
would not be shared across instances. A production implementation would persist it
with a TTL and would also store a hash of the request body, so that reusing a key
with a *different* payload is rejected (`422`) rather than silently returning an
unrelated order.

## Optimistic concurrency

`PUT /orders/{id}` requires an `If-Match` header carrying the ETag the client last
saw. If it does not match the current version, the write is rejected with
`412 Precondition Failed` and the stored order is left untouched. This prevents the
lost-update problem where two clients read the same order and the second write
silently discards the first.

> If the update endpoint is not present in your submission, delete the section above
> and the corresponding test 12 from `TestReport.pdf`, and record instead:
> *412 Precondition Failed is documented in the API design but was not implemented
> because the project does not currently expose an update endpoint requiring
> optimistic concurrency control.*

## Rate limiting

Every response carries `X-RateLimit-Limit` and `X-RateLimit-Remaining`. The counter is
per-process and in-memory; no `429 Too Many Requests` path was exercised during
testing because the limit of 100 was never reached.

Note that the `201` response in test 2 carried duplicate `X-RateLimit-*` headers.
Two components are writing them — probably both a filter and a controller-level
interceptor. One of the two should be removed.

## CORS

Responses include `Vary: Origin`, `Vary: Access-Control-Request-Method` and
`Vary: Access-Control-Request-Headers`, so caches do not serve a response prepared
for one origin to another.

## Not covered

- `429 Too Many Requests` — the rate limiter is implemented but the threshold was
  not crossed during testing.
- Pagination on `GET /orders`.
- Authentication beyond a static bearer token; `demo` is accepted verbatim and there
  is no signature check, expiry or user identity.