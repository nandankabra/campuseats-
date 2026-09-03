package com.example.campuseatsOrders.controller;
import com.example.campuseatsOrders.client.PaymentClient;
import com.example.campuseatsOrders.service.OrderService;
import com.example.campuseatsOrders.store.OrderStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import({
        OrderService.class,
        OrderStore.class
})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentClient paymentClient;

    @Test
    void createOrderReturns201AndLocationHeader() throws Exception {

        String requestBody = """
                {
                  "customerId": "CUST-101",
                  "items": [
                    {
                      "menuItemId": "MENU-501",
                      "quantity": 2
                    }
                  ]
                }
                """;

        mockMvc.perform(
                        post("/orders")
                                .header(
                                        "Idempotency-Key",
                                        "test-create-001"
                                )
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerId")
                        .value("CUST-101"))
                .andExpect(jsonPath("$.status")
                        .value("PLACED"));
    }

    @Test
    void repeatedIdempotencyKeyReturnsOriginalOrder()
            throws Exception {

        String requestBody = """
                {
                  "customerId": "CUST-102",
                  "items": [
                    {
                      "menuItemId": "MENU-502",
                      "quantity": 1
                    }
                  ]
                }
                """;

        String firstResponse =
                mockMvc.perform(
                                post("/orders")
                                        .header(
                                                "Idempotency-Key",
                                                "test-idempotent-001"
                                        )
                                        .contentType("application/json")
                                        .content(requestBody)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String secondResponse =
                mockMvc.perform(
                                post("/orders")
                                        .header(
                                                "Idempotency-Key",
                                                "test-idempotent-001"
                                        )
                                        .contentType("application/json")
                                        .content(requestBody)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        assertEquals(firstResponse, secondResponse);
    }

    @Test
    void malformedBodyReturns400WithProblemShape()
            throws Exception {

        mockMvc.perform(
                        post("/orders")
                                .header(
                                        "Idempotency-Key",
                                        "test-bad-request-001"
                                )
                                .contentType("application/json")
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void unknownOrderReturns404WithProblemShape()
            throws Exception {

        mockMvc.perform(
                        get("/orders/999999")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title")
                        .value("Order not found"))
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.detail")
                        .exists());
    }
}