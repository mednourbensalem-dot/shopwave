package com.shopwave;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ShopFlowTest {
    @Autowired MockMvc mvc;

    @Test
    void storefrontIsPublicAndServedFromRoot() throws Exception {
        mvc.perform(get("/"))
           .andExpect(status().isOk());
        mvc.perform(get("/index.html"))
           .andExpect(status().isOk())
           .andExpect(content().string(org.hamcrest.Matchers.containsString("Find your")));
        mvc.perform(get("/css/store.css"))
           .andExpect(status().isOk())
           .andExpect(content().string(org.hamcrest.Matchers.containsString(".product-grid")));
        mvc.perform(get("/js/store.js"))
           .andExpect(status().isOk())
           .andExpect(content().string(org.hamcrest.Matchers.containsString("/api/products")));
    }

    @Test
    void catalogIsPublic() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void cartRequiresLogin() throws Exception {
        mvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotUseAdminEndpoints() throws Exception {
        mvc.perform(get("/api/admin/stats").with(httpBasic("customer@shop.com", "customer123")))
           .andExpect(status().isForbidden());
    }

    @Test
    void customerCanAddToCartAndCheckout() throws Exception {
        mvc.perform(post("/api/cart/items").with(httpBasic("customer@shop.com", "customer123"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"productId\":1,\"quantity\":2}"))
           .andExpect(status().isOk());
        mvc.perform(post("/api/orders/checkout").with(httpBasic("customer@shop.com", "customer123"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"shippingAddress\":\"1 Main Street\"}"))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
