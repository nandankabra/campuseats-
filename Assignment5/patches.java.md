# Code changes needed to make the report accurate

Drop these into your existing package and adjust the package declaration.

## 1. 404 instead of 400 for a missing order

```java
// OrderNotFoundException.java
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException() {
        super("Order not found");
    }
}
```

In the service, throw it instead of a bare `RuntimeException`:

```java
public Order get(Long id) {
    Order order = orders.get(id);
    if (order == null) {
        throw new OrderNotFoundException();
    }
    return order;
}
```

In the `@RestControllerAdvice`:

```java
@ExceptionHandler(OrderNotFoundException.class)
public ResponseEntity<Problem> handleNotFound(OrderNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new Problem("Not Found", 404, ex.getMessage()));
}
```

Keep the existing `400` handler for genuine validation failures
(`MethodArgumentNotValidException`, `HttpMessageNotReadableException`). Do not
catch plain `RuntimeException` and map it to 404 — that turns every internal bug
into a "missing resource" and hides real failures.

## 2. Stable ETag

Your two GETs on the same unchanged order returned different ETags, which makes
304 unreachable. Compute it from the persisted fields only:

```java
private String etagOf(Order o) {
    return "\"" + Objects.hash(
            o.getOrderId(),
            o.getCustomerName(),
            o.getTotalAmount(),
            o.getStatus()) + "\"";
}
```

Then in the controller:

```java
@GetMapping("/orders/{id}")
public ResponseEntity<Order> getOrder(
        @PathVariable Long id,
        @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

    Order order = service.get(id);
    String etag = etagOf(order);

    if (etag.equals(ifNoneMatch)) {
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                .build();
    }

    return ResponseEntity.ok()
            .eTag(etag)
            .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
            .body(order);
}
```

`ResponseEntity.eTag(...)` expects the quotes to already be present in the string,
which `etagOf` above supplies.

## 3. PUT with If-Match → 412

```java
@PutMapping("/orders/{id}")
public ResponseEntity<?> updateOrder(
        @PathVariable Long id,
        @RequestHeader(value = "If-Match", required = false) String ifMatch,
        @RequestBody @Valid OrderRequest body) {

    Order current = service.get(id);          // throws OrderNotFoundException -> 404

    if (ifMatch == null) {
        return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED)
                .body(new Problem("Precondition Required", 428,
                        "If-Match header is required for updates"));
    }

    String etag = etagOf(current);
    if (!etag.equals(ifMatch)) {
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                .body(new Problem("Precondition Failed", 412,
                        "ETag does not match the current version"));
    }

    Order updated = service.update(id, body);
    return ResponseEntity.ok()
            .eTag(etagOf(updated))
            .body(updated);
}
```

If you would rather not add an update endpoint at all, delete test 12 from the
report and use the Option B note recorded in `NOTES.md`.

## 4. Idempotency-Key replay returning 200

```java
private final Map<String, Long> idempotencyKeys = new ConcurrentHashMap<>();

@PostMapping("/orders")
public ResponseEntity<Order> createOrder(
        @RequestHeader(value = "Idempotency-Key", required = false) String key,
        @RequestBody @Valid OrderRequest body) {

    if (key != null) {
        Long existingId = idempotencyKeys.get(key);
        if (existingId != null) {
            // replay: return the original resource, do not create a duplicate
            return ResponseEntity.ok(service.get(existingId));
        }
    }

    Order created = service.create(body);
    if (key != null) {
        idempotencyKeys.put(key, created.getOrderId());
    }

    return ResponseEntity.created(URI.create("/orders/" + created.getOrderId()))
            .body(created);
}
```

## 5. Duplicate rate-limit headers

The 201 response in test 2 carried `X-RateLimit-Limit` and `X-RateLimit-Remaining`
twice. Two components are setting them. Find whichever of your `OncePerRequestFilter`
and your `HandlerInterceptor` (or controller code) also writes them, and remove one.
Use `response.setHeader(...)`, not `response.addHeader(...)` — `addHeader` appends
rather than replacing.
