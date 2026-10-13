#!/usr/bin/env bash
set -e

OUTPUT="/Users/nandankabra/Desktop/IIIT /sem 3 /webservices/lab/Assignment6/transcript.txt"
BASE_URL="http://localhost:8080"
AUTH="Authorization: Bearer demo"

echo "================================================================================" > "$OUTPUT"
echo "Assignment 6 - CampusEats: Honest Responses, Status Codes, Errors & Validation" >> "$OUTPUT"
echo "Host: macOS (Darwin arm64) | Working Directory: Assignment6" >> "$OUTPUT"
echo "Target: $BASE_URL | Date: $(date)" >> "$OUTPUT"
echo "================================================================================" >> "$OUTPUT"
echo "" >> "$OUTPUT"

run_and_log() {
  local num="$1"
  local title="$2"
  local cmd_display="$3"
  local tmp_file="/tmp/curl_out_$$.txt"

  echo "--------------------------------------------------------------------------------" >> "$OUTPUT"
  echo "$num. $title" >> "$OUTPUT"
  echo "--------------------------------------------------------------------------------" >> "$OUTPUT"
  echo "$ $cmd_display" >> "$OUTPUT"
  echo "" >> "$OUTPUT"

  eval "$cmd_display" > "$tmp_file" 2>&1 || true
  cat "$tmp_file" >> "$OUTPUT"
  echo "" >> "$OUTPUT"
  echo "" >> "$OUTPUT"
  rm -f "$tmp_file"
}

# 1. Unauthenticated Request
run_and_log "1" "Authentication Failure - Missing Bearer Token (401 Unauthorized)" \
  "curl -s -S -i $BASE_URL/orders"

# 2. List orders initial state
run_and_log "2" "List Orders - Active state (200 OK)" \
  "curl -s -S -i -H \"$AUTH\" $BASE_URL/orders"

# 3. Happy Path: Create Order (D1)
UNIQUE_KEY="idem-create-$(date +%s)"
run_and_log "3" "Happy Path - Create Order (201 Created with Location header & JSON body)" \
  "curl -s -S -i -H \"$AUTH\" -H \"Content-Type: application/json\" -H \"Idempotency-Key: $UNIQUE_KEY\" -d '{\"customerName\":\"Nandan Kabra\",\"address\":\"Campus Hostel Block B\",\"items\":[{\"name\":\"Chicken Burger\",\"qty\":2,\"price\":150},{\"name\":\"French Fries\",\"qty\":1,\"price\":80}]}' -X POST $BASE_URL/orders"

ORDER_ID=$(grep -i "Location:" "$OUTPUT" | tail -n 1 | awk -F'/orders/' '{print $2}' | tr -d '\r\n[:space:]')
if [ -z "$ORDER_ID" ]; then ORDER_ID="1"; fi

# 4. Idempotent Replay (D4 No Regressions)
run_and_log "4" "Idempotency Replay - Re-send identical request with same key (200 OK, not 201)" \
  "curl -s -S -i -H \"$AUTH\" -H \"Content-Type: application/json\" -H \"Idempotency-Key: $UNIQUE_KEY\" -d '{\"customerName\":\"Nandan Kabra\",\"address\":\"Campus Hostel Block B\",\"items\":[{\"name\":\"Chicken Burger\",\"qty\":2,\"price\":150},{\"name\":\"French Fries\",\"qty\":1,\"price\":80}]}' -X POST $BASE_URL/orders"

# 5. Get Order by ID (200 OK with ETag & Cache-Control)
run_and_log "5" "Get Order by ID (200 OK with deterministic ETag & Cache-Control: max-age=60)" \
  "curl -s -S -i -H \"$AUTH\" $BASE_URL/orders/$ORDER_ID"

ETAG=$(grep -i "ETag:" "$OUTPUT" | tail -n 1 | awk '{print $2}' | tr -d '\r\n[:space:]')

# 6. Conditional GET with matching If-None-Match
run_and_log "6" "Conditional Request - Matching ETag (304 Not Modified)" \
  "curl -s -S -i -H \"$AUTH\" -H 'If-None-Match: $ETAG' $BASE_URL/orders/$ORDER_ID"

# 7. Content Negotiation - XML (D3 & Question 7)
run_and_log "7" "Content Negotiation - Accept: application/xml (200 OK with XML representation)" \
  "curl -s -S -i -H \"$AUTH\" -H \"Accept: application/xml\" $BASE_URL/orders/$ORDER_ID"

# 8. Content Negotiation - JSON (D3 & Question 7)
run_and_log "8" "Content Negotiation - Accept: application/json (200 OK with JSON representation)" \
  "curl -s -S -i -H \"$AUTH\" -H \"Accept: application/json\" $BASE_URL/orders/$ORDER_ID"

# 9. Content Negotiation - Unsupported Type (D3)
run_and_log "9" "Content Negotiation - Accept: text/csv (406 Not Acceptable)" \
  "curl -s -S -i -H \"$AUTH\" -H \"Accept: text/csv\" $BASE_URL/orders/$ORDER_ID"

# 10. PATCH Order - Update address & confirm status
run_and_log "10" "PATCH Order - Update delivery address & set status CONFIRMED (200 OK)" \
  "curl -s -S -i -H \"$AUTH\" -H \"Content-Type: application/json\" -d '{\"status\":\"CONFIRMED\",\"address\":\"Room 204, Campus Hostel Block B\"}' -X PATCH $BASE_URL/orders/$ORDER_ID"

# 11. Field-level validation error reporting TWO bad fields (B3, C2, Question 4)
run_and_log "11" "Field-Level Validation Error - Empty address & qty <= 0 (422 Unprocessable Entity)" \
  "curl -s -S -i -H \"$AUTH\" -H \"Content-Type: application/json\" -d '{\"customerName\":\"Nandan Kabra\",\"address\":\"\",\"items\":[{\"name\":\"Burger\",\"qty\":0,\"price\":100}]}' -X POST $BASE_URL/orders"

# 12. Empty cart failure (B2, D2)
run_and_log "12" "Cart Validation - Empty items array (422 Unprocessable Entity, empty-cart)" \
  "curl -s -S -i -H \"$AUTH\" -H \"Content-Type: application/json\" -d '{\"customerName\":\"Nandan Kabra\",\"address\":\"Campus Hostel\",\"items\":[]}' -X POST $BASE_URL/orders"

# 13. Item unavailable failure (B2, D2)
run_and_log "13" "Inventory Check - Item out of stock (409 Conflict, item-unavailable)" \
  "curl -s -S -i -H \"$AUTH\" -H \"Content-Type: application/json\" -d '{\"customerName\":\"Nandan Kabra\",\"address\":\"Campus Hostel\",\"items\":[{\"name\":\"OUT_OF_STOCK Burger\",\"qty\":1,\"price\":120}]}' -X POST $BASE_URL/orders"

# 14. Payment declined failure (B2, D2)
run_and_log "14" "Payment Processing - Card declined (422 Unprocessable Entity, payment-declined)" \
  "curl -s -S -i -H \"$AUTH\" -H \"Content-Type: application/json\" -d '{\"customerName\":\"Nandan Kabra\",\"address\":\"Campus Hostel\",\"items\":[{\"name\":\"Burger\",\"qty\":1,\"price\":100}],\"paymentMethod\":\"DECLINED\"}' -X POST $BASE_URL/orders"

# 15. Order not found failure (B2, D2)
run_and_log "15" "Resource Lookup - Non-existent order ID (404 Not Found, order-not-found)" \
  "curl -s -S -i -H \"$AUTH\" $BASE_URL/orders/9999"

# 16. Illegal state transition (B2, D2)
run_and_log "16" "State Machine - Transition CONFIRMED back to CREATED (409 Conflict, illegal-transition)" \
  "curl -s -S -i -H \"$AUTH\" -H \"Content-Type: application/json\" -d '{\"status\":\"CREATED\"}' -X PATCH $BASE_URL/orders/$ORDER_ID"

# 17. Cancel Order via PATCH
run_and_log "17" "State Machine - Transition CONFIRMED to CANCELLED (200 OK)" \
  "curl -s -S -i -H \"$AUTH\" -H \"Content-Type: application/json\" -d '{\"status\":\"CANCELLED\"}' -X PATCH $BASE_URL/orders/$ORDER_ID"

# 18. Illegal transition from CANCELLED (B2, D2)
run_and_log "18" "State Machine - Attempt to re-confirm CANCELLED order (409 Conflict, illegal-transition)" \
  "curl -s -S -i -H \"$AUTH\" -H \"Content-Type: application/json\" -d '{\"status\":\"CONFIRMED\"}' -X PATCH $BASE_URL/orders/$ORDER_ID"

# 19. Rate Limiting with Retry-After (A4, D2)
run_and_log "19" "Rate Limiting - Threshold exceeded (429 Too Many Requests with Retry-After: 60)" \
  "curl -s -S -i -H \"$AUTH\" -H \"X-Simulate-Rate-Limit: true\" $BASE_URL/orders"

# 20. Internal error masked without leaking internals (B4, Question 6)
run_and_log "20" "Internal Error Masking - Simulated SQL/replica failure (500 Internal Server Error)" \
  "curl -s -S -i -H \"$AUTH\" -H \"Content-Type: application/json\" -d '{\"customerName\":\"Nandan Kabra\",\"address\":\"Campus Hostel\",\"items\":[{\"name\":\"Burger\",\"qty\":1,\"price\":100}],\"simulateInternalError\":true}' -X POST $BASE_URL/orders"

# 21. Delete Order (204 No Content)
run_and_log "21" "Delete Order - Successful deletion (204 No Content)" \
  "curl -s -S -i -H \"$AUTH\" -X DELETE $BASE_URL/orders/$ORDER_ID"

# 22. Delete Order already deleted (404 Not Found)
run_and_log "22" "Delete Order - Repeated deletion (404 Not Found, order-not-found)" \
  "curl -s -S -i -H \"$AUTH\" -X DELETE $BASE_URL/orders/$ORDER_ID"

# 23. Preflight OPTIONS
run_and_log "23" "CORS / Method Discovery - OPTIONS /orders (204 No Content with Allow header)" \
  "curl -s -S -i -H \"$AUTH\" -X OPTIONS $BASE_URL/orders"

echo "Transcript generated successfully in $OUTPUT."
