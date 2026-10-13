package com.example.campuseatshttp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String AUTH_HEADER = "Bearer test-token";

    @Test
    @DisplayName("Part A & D1: Happy Path - Create order returns 201 Created with Location header and JSON body")
    void testCreateOrderHappyPath() throws Exception {
        String payload = """
            {
                "customerName": "Nandan Kabra",
                "address": "Campus Hostel Block B",
                "items": [
                    { "name": "Chicken Burger", "qty": 2, "price": 150 },
                    { "name": "French Fries", "qty": 1, "price": 80 }
                ]
            }
            """;

        mockMvc.perform(post("/orders")
                        .header("Authorization", AUTH_HEADER)
                        .header("Idempotency-Key", "test-key-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/orders/")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.customerName").value("Nandan Kabra"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(380));
    }

    @Test
    @DisplayName("Part A & D4: Idempotency Replay - Repeated key returns 200 OK (not 201) with same resource")
    void testIdempotencyReplay() throws Exception {
        String payload = """
            {
                "customerName": "Nandan Kabra",
                "address": "Campus Hostel Block B",
                "items": [
                    { "name": "Burger", "qty": 1, "price": 100 }
                ]
            }
            """;

        String key = "test-replay-key-02";

        // First creation -> 201 Created
        mockMvc.perform(post("/orders")
                        .header("Authorization", AUTH_HEADER)
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        // Replay -> 200 OK
        mockMvc.perform(post("/orders")
                        .header("Authorization", AUTH_HEADER)
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.customerName").value("Nandan Kabra"));
    }

    @Test
    @DisplayName("Part B3 & C2: Field-level 422 validation reporting two bad fields at once")
    void testValidationMultipleErrors() throws Exception {
        String invalidPayload = """
            {
                "customerName": "Nandan Kabra",
                "address": "",
                "items": [
                    { "name": "Burger", "qty": 0, "price": 100 }
                ]
            }
            """;

        mockMvc.perform(post("/orders")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.type").value("validation-error"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.errors", hasSize(2)))
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("address", "items[0].qty")));
    }

    @Test
    @DisplayName("Part B2: Empty cart returns 422 with type 'empty-cart'")
    void testEmptyCartFailure() throws Exception {
        String emptyCartPayload = """
            {
                "customerName": "Nandan Kabra",
                "address": "Campus Hostel",
                "items": []
            }
            """;

        mockMvc.perform(post("/orders")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyCartPayload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.type").value("empty-cart"))
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("Part B2: Item out of stock returns 409 Conflict with type 'item-unavailable'")
    void testItemUnavailableFailure() throws Exception {
        String payload = """
            {
                "customerName": "Nandan Kabra",
                "address": "Campus Hostel",
                "items": [
                    { "name": "OUT_OF_STOCK Fries", "qty": 1, "price": 50 }
                ]
            }
            """;

        mockMvc.perform(post("/orders")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.type").value("item-unavailable"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Part B2: Payment decline returns 422 with type 'payment-declined'")
    void testPaymentDeclinedFailure() throws Exception {
        String payload = """
            {
                "customerName": "Nandan Kabra",
                "address": "Campus Hostel",
                "items": [
                    { "name": "Pizza", "qty": 1, "price": 250 }
                ],
                "paymentMethod": "DECLINED"
            }
            """;

        mockMvc.perform(post("/orders")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.type").value("payment-declined"))
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("Part B2 & A1: Non-existent order returns 404 with type 'order-not-found'")
    void testOrderNotFound() throws Exception {
        mockMvc.perform(get("/orders/99999")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.type").value("order-not-found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Part C3 & D3: Content negotiation - Accept application/xml returns XML representation")
    void testContentNegotiationXml() throws Exception {
        String payload = """
            {
                "customerName": "Priya",
                "address": "Hostel 3",
                "items": [
                    { "name": "Sandwich", "qty": 1, "price": 60 }
                ]
            }
            """;

        MvcResult createResult = mockMvc.perform(post("/orders")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String location = createResult.getResponse().getHeader("Location");

        mockMvc.perform(get(location)
                        .header("Authorization", AUTH_HEADER)
                        .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(xpath("/order/customerName").string(notNullValue()))
                .andExpect(xpath("/order/status").string(notNullValue()));
    }

    @Test
    @DisplayName("Part C3 & D3: Content negotiation - Unsupported Accept returns 406 Not Acceptable")
    void testContentNegotiationUnsupported() throws Exception {
        mockMvc.perform(get("/orders/1")
                        .header("Authorization", AUTH_HEADER)
                        .accept("text/csv"))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    @DisplayName("Part B2: Illegal state transition returns 409 Conflict with type 'illegal-transition'")
    void testIllegalStateTransition() throws Exception {
        String payload = """
            {
                "customerName": "Test Transition",
                "address": "Campus Hostel",
                "items": [
                    { "name": "Burger", "qty": 1, "price": 100 }
                ]
            }
            """;

        MvcResult createResult = mockMvc.perform(post("/orders")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String location = createResult.getResponse().getHeader("Location");

        // Cancel the order
        mockMvc.perform(patch(location)
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk());

        // Try transitioning CANCELLED back to CONFIRMED -> 409 Conflict
        mockMvc.perform(patch(location)
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isConflict())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.type").value("illegal-transition"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Part B4: Internal error masking returns clean 500 without leaking internals")
    void testInternalErrorMasking() throws Exception {
        String payload = """
            {
                "customerName": "Neeraj",
                "address": "Campus Hostel",
                "items": [
                    { "name": "Wrap", "qty": 1, "price": 90 }
                ],
                "simulateInternalError": true
            }
            """;

        mockMvc.perform(post("/orders")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.type").value("internal-error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value(not(containsString("SQLException"))))
                .andExpect(jsonPath("$.detail").value(not(containsString("db-shard"))));
    }

    @Test
    @DisplayName("Part A4: Rate limiting simulation returns 429 with Retry-After header")
    void testRateLimiting() throws Exception {
        mockMvc.perform(get("/orders")
                        .header("Authorization", AUTH_HEADER)
                        .header("X-Simulate-Rate-Limit", "true"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.type").value("rate-limit-exceeded"))
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    @DisplayName("Part A1: DELETE returns 204 No Content, repeated DELETE returns 404")
    void testDeleteOrder() throws Exception {
        String payload = """
            {
                "customerName": "Abhinav",
                "address": "Campus Hostel",
                "items": [
                    { "name": "Coffee", "qty": 1, "price": 40 }
                ]
            }
            """;

        MvcResult createResult = mockMvc.perform(post("/orders")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String location = createResult.getResponse().getHeader("Location");

        // Delete order
        mockMvc.perform(delete(location)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isNoContent());

        // Repeated delete -> 404
        mockMvc.perform(delete(location)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("order-not-found"));
    }
}
